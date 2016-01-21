package unife.icedroid;

import unife.icedroid.core.Intent;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.BoxLayout;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;

public class ICeNUXWindow {
	public static void main(String[] args) {
		Settings s = Settings.getSettings();
		if (s == null) System.exit(-1);
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				start();
			}
			
		});
	}
	
	private static void start() {
		final JFrame window = new JFrame();
		window.setTitle("ICeNUX");
		window.setSize(400, 500);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				Settings.getSettings().close();
				window.dispose();
				System.exit(0);
			}
			
		});
		
		JPanel toolbar = new JPanel(new BorderLayout());
		JLabel title = new JLabel("ICeNUX");
		JPanel newChatContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton newChat = new JButton(new ImageIcon("resources/ic_chat.png"));
		newChat.setPreferredSize(new Dimension(50, 50));
		newChat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final JDialog dialog = new JDialog(window, Dialog.ModalityType.APPLICATION_MODAL);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setTitle("Crea Chat");
				dialog.setLocationRelativeTo(window);
				
				JPanel panel = new JPanel();
				BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
				panel.setLayout(layout);
				
				final JTextField channel = new JTextField(25);
				channel.setText("Nome del canale");
				final JTextField group = new JTextField(25);
				group.setText("Nome del gruppo");
				panel.add(channel);
				panel.add(group);
				
				JButton save = new JButton("Salva");
				save.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						String channelID = channel.getText();
				        String groupName = group.getText();
				        Subscription subscription = SubscriptionListManager.
				        						getSubscriptionListManager().
				        						subscribe(channelID, groupName);
				        
				        Intent intent = new Intent();
				        intent.putExtra(Subscription.EXTRA_SUBSCRIPTION, subscription);
				        ChatWindow.open(subscription);
				        dialog.dispose();
					}
					
				});
				
				dialog.add(panel, BorderLayout.CENTER);
				dialog.add(save, BorderLayout.SOUTH);
				dialog.pack();
				dialog.setResizable(false);
				dialog.setVisible(true);
			}
			
		});
		
		newChatContainer.add(newChat);
		toolbar.add(title, BorderLayout.WEST);
		toolbar.add(newChatContainer, BorderLayout.EAST);
		
		final DefaultListModel<String> listData = new DefaultListModel<>();
		for (Subscription s : SubscriptionListManager.getSubscriptionListManager().getSubscriptionsList()) {
			listData.addElement(s.toString());
		}
		JList<String> list = new JList<>(listData);
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					JList<String> list = (JList<String>) e.getSource();
					String sub = list.getSelectedValue();
					if (sub != null) {
						String[] chAndgr = sub.split(":");
						Subscription subscription = new Subscription(chAndgr[0], chAndgr[1]);
						
						Intent intent = new Intent();
				        intent.putExtra(Subscription.EXTRA_SUBSCRIPTION, subscription);
				        ChatWindow.open(subscription);
					}
				}				
			}
		});
		JScrollPane listContainer = new JScrollPane(list);
		listContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);		
		
		window.add(toolbar, BorderLayout.NORTH);
		window.add(listContainer, BorderLayout.CENTER);
		window.setVisible(true);
		
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			
			@Override
			protected Void doInBackground() {
				Path subDir = Paths.get("resources/");
				
				try {
					WatchService watcher = subDir.getFileSystem().newWatchService();
					subDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					
					while (true) {
						WatchKey wk = watcher.take();
						for (WatchEvent<?> event : wk.pollEvents()) {
							Path file = (Path) event.context();
							if (file.endsWith("subscriptions")) {
								publish(SubscriptionListManager.
										getSubscriptionListManager().
										getLastSubscription().
										toString());
							}
						}
						wk.reset();
					}
					
				} catch (Exception ex) {
					System.out.println(ex); 
				}
				
				return null;
			}
			
			@Override
			protected void process(List<String> list) {
				for (String s : list) {
					listData.addElement(s);
				}
			}
			
		};
		worker.execute();
	}
}
