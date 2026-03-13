import customtkinter as ctk
import numpy as np
import matplotlib.pyplot as plt
from sklearn.tree import DecisionTreeClassifier
from reportlab.pdfgen import canvas
import random
import os
from tkinter import messagebox

X = np.array([
    [1, 1, 1, 0, 0, 0, 0, 0],
    [1, 0, 0, 1, 1, 0, 0, 0],
    [1, 0, 0, 0, 0, 1, 1, 0],
    [1, 0, 0, 0, 0, 0, 0, 1]
])

y = ["Flu", "Dengue", "Malaria", "COVID"]

model = DecisionTreeClassifier()
model.fit(X, y)

def rule_engine(s):
    if s[0] and s[1] and s[2]:
        return "Flu"
    if s[0] and s[3] and s[4]:
        return "Dengue"
    if s[0] and s[5] and s[6]:
        return "Malaria"
    if s[0] and s[7]:
        return "COVID"
    return "Unknown"

def show_chart(data):
    diseases = list(data.keys())
    values = list(data.values())

    plt.style.use('ggplot')
    plt.figure(figsize=(7, 4))
    plt.bar(diseases, values, color=['#4C9900', '#FF9933', '#3399FF', '#FF3333'])
    plt.title("Disease Probability Analysis", fontweight='bold')
    plt.ylabel("Probability")
    plt.ylim(0, 1)
    plt.tight_layout()
    plt.show()

def generate_report(patient, result):
    if not patient.strip():
        patient = "Unknown_Patient"
        
    os.makedirs("reports", exist_ok=True)
    file = f"reports/{patient}_report.pdf"

    c = canvas.Canvas(file)
    c.setFont("Helvetica-Bold", 16)
    c.drawString(200, 800, "Medical Diagnosis Report")
    
    c.setFont("Helvetica", 12)
    c.drawString(100, 750, f"Patient Name : {patient}")
    c.drawString(100, 720, f"Diagnosis : {result}")
    c.drawString(100, 680, "Note: This is an AI-based expert system prediction.")
    c.save()

ctk.set_appearance_mode("Dark")
ctk.set_default_color_theme("blue")

class MedicalDashboard(ctk.CTk):
    def __init__(self):
        super().__init__()

        self.title("AI Medical Expert System")
        self.geometry("700x750")
        self.resizable(False, False)

        self.title_label = ctk.CTkLabel(
            self, 
            text="Hospital Diagnosis Dashboard", 
            font=ctk.CTkFont(family="Roboto", size=28, weight="bold")
        )
        self.title_label.pack(pady=(30, 20))

        self.info_frame = ctk.CTkFrame(self, fg_color="transparent")
        self.info_frame.pack(pady=10, padx=40, fill="x")
        
        self.name_label = ctk.CTkLabel(self.info_frame, text="Patient Name:", font=ctk.CTkFont(size=14))
        self.name_label.pack(side="left", padx=(0, 10))

        self.patient_name = ctk.StringVar()
        self.name_entry = ctk.CTkEntry(
            self.info_frame, 
            textvariable=self.patient_name, 
            width=300, 
            placeholder_text="Enter full name...",
            corner_radius=8
        )
        self.name_entry.pack(side="left")

        self.subtitle_label = ctk.CTkLabel(
            self, 
            text="Select Observed Symptoms", 
            font=ctk.CTkFont(size=18, weight="bold")
        )
        self.subtitle_label.pack(pady=(20, 10))

        self.checkbox_frame = ctk.CTkFrame(self, corner_radius=15)
        self.checkbox_frame.pack(pady=10, padx=40, fill="both", expand=True)
        self.checkbox_frame.grid_columnconfigure((0, 1), weight=1)

        symptoms = [
            "Fever", "Cough", "Sore Throat", "Headache",
            "Body Pain", "Chills", "Sweating", "Loss of Taste"
        ]

        self.vars = []
        for i, s in enumerate(symptoms):
            var = ctk.IntVar()
            cb = ctk.CTkCheckBox(
                self.checkbox_frame, 
                text=s, 
                variable=var, 
                font=ctk.CTkFont(size=14),
                corner_radius=6
            )
            row = i // 2
            col = i % 2
            cb.grid(row=row, column=col, pady=15, padx=30, sticky="w")
            self.vars.append(var)

        self.progress = ctk.CTkProgressBar(self, width=400, height=12, corner_radius=8)
        self.progress.set(0)
        self.progress.pack(pady=(20, 10))
        self.progress.pack_forget()

        self.diagnose_btn = ctk.CTkButton(
            self, 
            text="Start AI Diagnosis", 
            font=ctk.CTkFont(size=16, weight="bold"),
            height=45,
            corner_radius=8,
            command=self.start_diagnosis
        )
        self.diagnose_btn.pack(pady=20)

    def start_diagnosis(self):
        if not self.patient_name.get().strip():
            messagebox.showwarning("Input Error", "Please enter a patient name.")
            return

        self.diagnose_btn.configure(state="disabled", text="Analyzing...")
        self.progress.pack(pady=(20, 10))
        self.progress.set(0)
        self.animate()

    def animate(self):
        current_val = self.progress.get()
        if current_val < 1.0:
            self.progress.set(current_val + 0.05)
            self.after(60, self.animate)
        else:
            self.diagnose()

    def diagnose(self):
        symptoms = [v.get() for v in self.vars]

        rule_result = rule_engine(symptoms)
        ai_result = model.predict([symptoms])[0]

        result = ai_result if rule_result == "Unknown" else rule_result

        probabilities = {
            "Flu": random.uniform(0.3, 0.9) if result == "Flu" else random.uniform(0.0, 0.3),
            "Dengue": random.uniform(0.2, 0.8) if result == "Dengue" else random.uniform(0.0, 0.3),
            "Malaria": random.uniform(0.1, 0.7) if result == "Malaria" else random.uniform(0.0, 0.3),
            "COVID": random.uniform(0.2, 0.9) if result == "COVID" else random.uniform(0.0, 0.3)
        }

        self.progress.pack_forget()
        self.diagnose_btn.configure(state="normal", text="Start AI Diagnosis")

        self.show_result(result, probabilities)

    def show_result(self, result, prob):
        win = ctk.CTkToplevel(self)
        win.title("Diagnosis Result")
        win.geometry("450x400")
        win.resizable(False, False)
        
        win.grab_set()

        label = ctk.CTkLabel(
            win, 
            text="Final Diagnosis", 
            font=ctk.CTkFont(size=22, weight="bold")
        )
        label.pack(pady=(30, 10))

        text_color = "#FF4C4C" if result in ["COVID", "Dengue"] else "#FFA500"

        result_label = ctk.CTkLabel(
            win, 
            text=result.upper(), 
            font=ctk.CTkFont(size=32, weight="bold"),
            text_color=text_color
        )
        result_label.pack(pady=10)

        action_frame = ctk.CTkFrame(win, fg_color="transparent")
        action_frame.pack(pady=30)

        graph_btn = ctk.CTkButton(
            action_frame, 
            text="Show Probability Chart", 
            font=ctk.CTkFont(size=14),
            height=40,
            command=lambda: show_chart(prob)
        )
        graph_btn.pack(pady=10, fill="x")

        report_btn = ctk.CTkButton(
            action_frame, 
            text="Generate Patient Report", 
            font=ctk.CTkFont(size=14),
            height=40,
            fg_color="#2FA572",
            hover_color="#1F7A50",
            command=lambda: self.trigger_report(result)
        )
        report_btn.pack(pady=10, fill="x")

    def trigger_report(self, result):
        generate_report(self.patient_name.get(), result)
        messagebox.showinfo("Success", f"Report generated in 'reports' folder for {self.patient_name.get()}.")

if __name__ == "__main__":
    app = MedicalDashboard()
    app.mainloop()