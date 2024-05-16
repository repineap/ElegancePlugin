import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFileCollector {

	public MainFileCollector() {
		JFrame frame = new JFrame("File Chooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        JTextField paramTextField = new JTextField("params:2.0,4.0,1.0,1.0,2.0,5.0");
        topPanel.add(paramTextField);
        
        JPanel panel = new JPanel();

        JButton fileAnalyzeButton = new JButton("Analyze student code");
        panel.add(fileAnalyzeButton);
        JButton setConfigButton = new JButton("Set config file");
        panel.add(setConfigButton);
        JButton configCreateButton = new JButton("Generate config files");
        panel.add(configCreateButton);
        
        frame.add(panel, BorderLayout.NORTH);
        frame.add(topPanel, BorderLayout.CENTER);

        fileAnalyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    new StudentAnalyzer(fileChooser.getSelectedFile());
                } else {
                    System.out.println("Open command canceled by user.");
                }
            }
        });
        
        configCreateButton.addActionListener(new ActionListener() {
			
        	 @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fileChooser = new JFileChooser();
                 fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                 int result = fileChooser.showOpenDialog(frame);
                 if (result == JFileChooser.APPROVE_OPTION) {
                     new ConfigGenerator(fileChooser.getSelectedFile(), paramTextField.getText());
                 } else {
                     System.out.println("Open command canceled by user.");
                 }
             }
		});

        frame.setVisible(true);
	}
	
	public MainFileCollector(String type, String filePath) {
		if (type.charAt(1) == 'a') {
			new StudentAnalyzer(new File(filePath));
		} else if (type.charAt(1) == 'c') {
			new ConfigGenerator(new File(filePath), null);
		} else {
			System.out.println("Invalid command");
		}
	}
	
    public static void main(String[] args) {
    	if (args.length == 1) {
    		if (args[0].charAt(1) == 'h') {
    			System.out.println("Type -a(nalyze) <filepath> or -c(onfig) <filepath> to use");
    		} else {
    			System.out.println("Missing filepath");
    		}
    	} else if (args.length == 2) {
    		new MainFileCollector(args[0], args[1]);
    	} else {
    		new MainFileCollector();
    	}
    }
}