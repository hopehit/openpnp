package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.support.WizardContainer;
import org.openpnp.gui.tablemodel.HeadsTableModel;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Head;

public class HeadsPanel extends JPanel implements WizardContainer {
	private final Frame frame;
	private final Configuration configuration;
	private final MachineControlsPanel machineControlsPanel;

	private JTable table;

	private HeadsTableModel tableModel;
	private TableRowSorter<HeadsTableModel> tableSorter;
	private JTextField searchTextField;
	JPanel configurationPanel;

	public HeadsPanel(Frame frame, Configuration configuration,
			MachineControlsPanel machineControlsPanel) {
		this.frame = frame;
		this.configuration = configuration;
		this.machineControlsPanel = machineControlsPanel;

		setLayout(new BorderLayout(0, 0));
		tableModel = new HeadsTableModel(configuration);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel.add(toolBar, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);

		JLabel lblSearch = new JLabel("Search");
		panel_1.add(lblSearch);

		searchTextField = new JTextField();
		searchTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				search();
			}
		});
		panel_1.add(searchTextField);
		searchTextField.setColumns(15);

		table = new JTable(tableModel);
		tableSorter = new TableRowSorter<HeadsTableModel>(tableModel);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(0.3);
		add(splitPane, BorderLayout.CENTER);

		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
		
		splitPane.setLeftComponent(new JScrollPane(table));
		splitPane.setRightComponent(configurationPanel);
		configurationPanel.setLayout(new BorderLayout(0, 0));
		table.setRowSorter(tableSorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}

						Head head = getSelectedHead();

						configurationPanel.removeAll();
						if (head != null) {
							Wizard wizard = head.getConfigurationWizard();
							if (wizard != null) {
								wizard.setWizardContainer(HeadsPanel.this);
								JPanel panel = wizard.getWizardPanel();
								configurationPanel.add(panel);
							}
						}
						revalidate();
						repaint();
					}
				});

	}

	private Head getSelectedHead() {
		int index = table.getSelectedRow();

		if (index == -1) {
			return null;
		}

		index = table.convertRowIndexToModel(index);
		return tableModel.getHead(index);
	}

	private void search() {
		RowFilter<HeadsTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try {
			rf = RowFilter.regexFilter("(?i)"
					+ searchTextField.getText().trim());
		}
		catch (PatternSyntaxException e) {
			System.out.println(e);
			return;
		}
		tableSorter.setRowFilter(rf);
	}

	@Override
	public void wizardCompleted(Wizard wizard) {
		configuration.setDirty(true);
	}

	@Override
	public void wizardCancelled(Wizard wizard) {
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public MachineControlsPanel getMachineControlsPanel() {
		return machineControlsPanel;
	}
}