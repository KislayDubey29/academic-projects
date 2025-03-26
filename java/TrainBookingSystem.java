import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

class Train {
    String trainNo, name, source, destination;
    int availableSeats;
    double baseFare;

    public Train(String trainNo, String name, String source, String destination, int availableSeats, double baseFare) {
        this.trainNo = trainNo;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.availableSeats = availableSeats;
        this.baseFare = baseFare;
    }
}

class Ticket {
    String pnr, passengerName, trainNo, date, quota, seatStatus;
    boolean autoUpgrade;
    double totalFare;

    public Ticket(String pnr, String passengerName, String trainNo, String date, String quota, boolean autoUpgrade, double totalFare, String seatStatus) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNo = trainNo;
        this.date = date;
        this.quota = quota;
        this.autoUpgrade = autoUpgrade;
        this.totalFare = totalFare;
        this.seatStatus = seatStatus;
    }
}

class BookingManager {
    private List<Train> trains;
    private final String TRAIN_FILE = "trains_data.csv";

    public BookingManager() {
        trains = new ArrayList<>();
        loadTrains();
    }

    public List<Train> searchTrains(String source, String destination) {
        List<Train> results = new ArrayList<>();
        for (Train t : trains) {
            if (t.source.equals(source) && t.destination.equals(destination)) {
                results.add(t);
            }
        }
        
        if (results.isEmpty()) {
            String randTrainNo = "1" + (1000 + new Random().nextInt(8000));
            int randSeats = 60 - new Random().nextInt(100); 
            double randFare = 800.0 + new Random().nextInt(2000);
            
            Train generatedTrain = new Train(randTrainNo, "FastTrack Special Exp", source, destination, randSeats, randFare);
            
            trains.add(generatedTrain);
            saveTrains(); 
            
            results.add(generatedTrain);
        }
        
        return results;
    }

    private void loadTrains() {
        File file = new File(TRAIN_FILE);
        if (!file.exists()) {
            trains.add(new Train("12004", "Shatabdi Exp", "New Delhi (NDLS)", "Lucknow (LKO)", 50, 1200.0));
            trains.add(new Train("12951", "Rajdhani Exp", "Mumbai (BCT)", "New Delhi (NDLS)", -15, 2500.0));
            trains.add(new Train("12296", "Sanghamitra Exp", "Bengaluru (SBC)", "Patna (PNBE)", -5, 950.0));
            trains.add(new Train("12841", "Coromandel Exp", "Howrah (HWH)", "Chennai (MAS)", 12, 1800.0));
            trains.add(new Train("12915", "Ashram Exp", "Ahmedabad (ADI)", "New Delhi (NDLS)", -45, 800.0));
            trains.add(new Train("12424", "Rajdhani Exp", "New Delhi (NDLS)", "Guwahati (GHY)", 4, 3200.0));
            trains.add(new Train("12149", "Pune Danapur Exp", "Pune (PUNE)", "Patna (PNBE)", -110, 850.0));
            saveTrains();
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    trains.add(new Train(data[0], data[1], data[2], data[3], Integer.parseInt(data[4]), Double.parseDouble(data[5])));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void saveTrains() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRAIN_FILE))) {
            for (Train t : trains) {
                bw.write(t.trainNo + "," + t.name + "," + t.source + "," + t.destination + "," + t.availableSeats + "," + t.baseFare);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public int getConfirmationProbability(int availableSeats) {
        if (availableSeats > 0) return 100;
        
        int wlNumber = Math.abs(availableSeats);
        int probability = 100 - (wlNumber * 3);
        return Math.max(5, Math.min(95, probability));
    }

    public double calculateFare(Train train, String quota) {
        double fare = train.baseFare;
        if (quota.equals("Tatkal")) fare += (fare * 0.30);
        else if (quota.equals("Premium Tatkal")) fare += (fare * 0.50);
        return fare + (fare * 0.05);
    }

    public Ticket bookTicket(String name, Train train, String date, String quota, boolean upgrade) {
        double finalFare = calculateFare(train, quota);
        String pnr = "PNR" + (100000 + new Random().nextInt(900000));
        
        String status;
        if (train.availableSeats > 0) {
            status = "CNF (Confirmed)";
        } else {
            status = "WL " + (Math.abs(train.availableSeats) + 1);
        }

        train.availableSeats--;
        saveTrains();
        
        return new Ticket(pnr, name, train.trainNo, date, quota, upgrade, finalFare, status);
    }
}

public class TrainBookingSystem extends JFrame {
    
    private BookingManager manager;
    private JComboBox<String> sourceCombo, destCombo, trainCombo, quotaCombo;
    private JComboBox<String> yearCombo, monthCombo, dayCombo;
    private JTextField nameField;
    private JCheckBox autoUpgradeCheck;
    private JLabel priceLabel, seatLabel;
    private JProgressBar predictBar; 
    private JTextArea receiptArea;
    private JButton bookButton;
    private List<Train> currentSearchResults;

    private final String[] MAJOR_STATIONS = {
        "New Delhi (NDLS) - Delhi", "Mumbai (BCT) - Maharashtra", "Pune (PUNE) - Maharashtra",
        "Lucknow (LKO) - UP", "Kanpur (CNB) - UP", "Varanasi (BSB) - UP",
        "Howrah (HWH) - West Bengal", "Patna (PNBE) - Bihar", "Gaya (GAYA) - Bihar",
        "Bengaluru (SBC) - Karnataka", "Chennai (MAS) - Tamil Nadu", "Hyderabad (HYB) - Telangana",
        "Ahmedabad (ADI) - Gujarat", "Surat (ST) - Gujarat", "Jaipur (JP) - Rajasthan",
        "Bhopal (BPL) - MP", "Indore (INDB) - MP", "Guwahati (GHY) - Assam",
        "Bhubaneswar (BBS) - Odisha", "Chandigarh (CDG) - Punjab/Haryana", "Thiruvananthapuram (TVC) - Kerala"
    };

    public TrainBookingSystem() {
        manager = new BookingManager();
        currentSearchResults = new ArrayList<>();
        setupUI();
    }

    private void setupUI() {
        setTitle("FastTrack - AI Enhanced eTicketing");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("FastTrack Railway Reservations", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headerLabel.setForeground(new Color(25, 55, 109));
        add(headerLabel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(420, 0));

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder(null, "1. Plan My Journey", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14)));
        
        Arrays.sort(MAJOR_STATIONS);
        sourceCombo = new JComboBox<>(MAJOR_STATIONS);
        destCombo = new JComboBox<>(MAJOR_STATIONS);
        destCombo.setSelectedIndex(2);
        
        searchPanel.add(new JLabel("From Station:")); searchPanel.add(sourceCombo);
        searchPanel.add(new JLabel("To Station:")); searchPanel.add(destCombo);
        
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        
        String[] days = new String[31];
        for(int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
        String[] months = new String[12];
        for(int i = 0; i < 12; i++) months[i] = String.format("%02d", i + 1);
        String[] years = {"2026", "2027"}; 

        yearCombo = new JComboBox<>(years);
        monthCombo = new JComboBox<>(months);
        dayCombo = new JComboBox<>(days);

        Calendar cal = Calendar.getInstance();
        monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
        dayCombo.setSelectedIndex(cal.get(Calendar.DAY_OF_MONTH) - 1);

        datePanel.add(yearCombo);
        datePanel.add(new JLabel("-"));
        datePanel.add(monthCombo);
        datePanel.add(new JLabel("-"));
        datePanel.add(dayCombo);

        searchPanel.add(new JLabel("Journey Date (Y-M-D):")); 
        searchPanel.add(datePanel);
        
        JButton searchButton = new JButton("Search Trains");
        searchButton.setBackground(new Color(255, 153, 0));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> executeSearch());
        searchPanel.add(new JLabel("")); searchPanel.add(searchButton);

        leftPanel.add(searchPanel);
        leftPanel.add(Box.createVerticalStrut(10));

        JPanel bookPanel = new JPanel(new GridLayout(8, 2, 5, 8)); 
        bookPanel.setBorder(BorderFactory.createTitledBorder(null, "2. Passenger & Ticket Details", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14)));

        trainCombo = new JComboBox<>();
        trainCombo.setEnabled(false);
        trainCombo.addActionListener(e -> updatePricingAndPrediction());
        
        String[] quotas = {"General", "Tatkal", "Premium Tatkal", "Ladies"};
        quotaCombo = new JComboBox<>(quotas);
        quotaCombo.addActionListener(e -> updatePricingAndPrediction());

        nameField = new JTextField();
        autoUpgradeCheck = new JCheckBox("Consider for Auto-Upgradation");

        seatLabel = new JLabel("Status: --");
        seatLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        predictBar = new JProgressBar(0, 100);
        predictBar.setStringPainted(true);
        predictBar.setString("Smart Predictor: N/A");
        
        priceLabel = new JLabel("Fare: ₹0.00");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        priceLabel.setForeground(new Color(0, 102, 0));

        bookPanel.add(new JLabel("Select Train:")); bookPanel.add(trainCombo);
        bookPanel.add(new JLabel("Passenger Name:")); bookPanel.add(nameField);
        bookPanel.add(new JLabel("Quota:")); bookPanel.add(quotaCombo);
        bookPanel.add(new JLabel("Options:")); bookPanel.add(autoUpgradeCheck);
        bookPanel.add(new JLabel("Availability:")); bookPanel.add(seatLabel);
        bookPanel.add(new JLabel("WL AI Prediction:")); bookPanel.add(predictBar);
        bookPanel.add(new JLabel("Est. Total:")); bookPanel.add(priceLabel);

        bookButton = new JButton("Book Ticket");
        bookButton.setBackground(new Color(46, 204, 113));
        bookButton.setEnabled(false);
        bookButton.addActionListener(e -> processBooking());
        bookPanel.add(new JLabel("")); bookPanel.add(bookButton);

        leftPanel.add(bookPanel);
        add(leftPanel, BorderLayout.WEST);

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "Electronic Reservation Slip (ERS)", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void executeSearch() {
        String src = (String) sourceCombo.getSelectedItem();
        String dest = (String) destCombo.getSelectedItem();
        
        if (src.equals(dest)) {
            JOptionPane.showMessageDialog(this, "Source and Destination cannot be the same!", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cleanSrc = src.split(" - ")[0];
        String cleanDest = dest.split(" - ")[0];

        currentSearchResults = manager.searchTrains(cleanSrc, cleanDest);
        trainCombo.removeAllItems();

        if (currentSearchResults.isEmpty()) {
            trainCombo.addItem("No Trains Found");
            trainCombo.setEnabled(false);
            bookButton.setEnabled(false);
            seatLabel.setText("Status: --");
            predictBar.setValue(0);
            predictBar.setString("Smart Predictor: N/A");
            priceLabel.setText("Fare: ₹0.00");
        } else {
            for (Train t : currentSearchResults) {
                trainCombo.addItem(t.trainNo + " - " + t.name);
            }
            trainCombo.setEnabled(true);
            bookButton.setEnabled(true);
            updatePricingAndPrediction();
        }
    }

    private void updatePricingAndPrediction() {
        if (trainCombo.getSelectedIndex() >= 0 && !currentSearchResults.isEmpty()) {
            Train selectedTrain = currentSearchResults.get(trainCombo.getSelectedIndex());
            String quota = (String) quotaCombo.getSelectedItem();
            
            if (selectedTrain.availableSeats > 0) {
                seatLabel.setText("Status: AVAILABLE (" + selectedTrain.availableSeats + ")");
                seatLabel.setForeground(new Color(0, 153, 76)); 
            } else {
                int wlNum = Math.abs(selectedTrain.availableSeats) + 1;
                seatLabel.setText("Status: WL " + wlNum);
                seatLabel.setForeground(Color.RED);
            }

            int prob = manager.getConfirmationProbability(selectedTrain.availableSeats);
            predictBar.setValue(prob);
            if (prob == 100) {
                predictBar.setString("Confirmed");
                predictBar.setForeground(new Color(46, 204, 113));
            } else if (prob > 60) {
                predictBar.setString(prob + "% Chance (Likely)");
                predictBar.setForeground(Color.ORANGE);
            } else {
                predictBar.setString(prob + "% Chance (Unlikely)");
                predictBar.setForeground(Color.RED);
            }

            double fare = manager.calculateFare(selectedTrain, quota);
            priceLabel.setText(String.format("Fare: ₹%.2f", fare));
        }
    }

    private void processBooking() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Passenger name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Train selectedTrain = currentSearchResults.get(trainCombo.getSelectedIndex());
        
        String selectedDate = yearCombo.getSelectedItem() + "-" + 
                              monthCombo.getSelectedItem() + "-" + 
                              dayCombo.getSelectedItem();

        Ticket t = manager.bookTicket(
            nameField.getText().trim(),
            selectedTrain,
            selectedDate, 
            (String) quotaCombo.getSelectedItem(),
            autoUpgradeCheck.isSelected()
        );

        StringBuilder receipt = new StringBuilder();
        receipt.append("==============================================\n");
        receipt.append("          INDIAN RAILWAYS E-TICKETING         \n");
        receipt.append("==============================================\n");
        receipt.append(String.format("%-20s %s\n", "PNR No:", t.pnr));
        receipt.append(String.format("%-20s %s\n", "Train No & Name:", selectedTrain.trainNo + " " + selectedTrain.name));
        receipt.append(String.format("%-20s %s\n", "Date of Journey:", t.date));
        receipt.append(String.format("%-20s %s\n", "Booking Status:", t.seatStatus));
        receipt.append("----------------------------------------------\n");
        receipt.append(String.format("%-20s %s\n", "From:", selectedTrain.source));
        receipt.append(String.format("%-20s %s\n", "To:", selectedTrain.destination));
        receipt.append("----------------------------------------------\n");
        receipt.append("PASSENGER DETAILS:\n");
        receipt.append("1. ").append(t.passengerName).append(" | Quota: ").append(t.quota).append("\n");
        receipt.append("Auto-Upgradation: ").append(t.autoUpgrade ? "Opted In (YES)" : "Opted Out (NO)").append("\n");
        receipt.append("----------------------------------------------\n");
        receipt.append(String.format("TOTAL FARE (incl. GST):  ₹%.2f\n", t.totalFare));
        receipt.append("==============================================\n");

        receiptArea.setText(receipt.toString());
        updatePricingAndPrediction(); 
        
        JOptionPane.showMessageDialog(this, "Booking Successful!\nStatus: " + t.seatStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> new TrainBookingSystem().setVisible(true));
    }
}