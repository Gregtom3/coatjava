package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.splot.pdata.Histo2DData;

public class DefineHisto2DDialog extends JDialog implements ActionListener, PropertyChangeListener {
	
	private JButton _okButton;
	private JButton _cancelButton;
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	private Histo2DPanel _histoPanel;
	
	//names resulting from selection
	private String _xName;
	private String _yName;

	public DefineHisto2DDialog() {
		setTitle("Define a 2D Histogram");
		setModal(true);
		setLayout(new BorderLayout(4, 4));
		
		_histoPanel = new Histo2DPanel();
		add(_histoPanel, BorderLayout.CENTER);

		_histoPanel.getSelectPanelX().addPropertyChangeListener(this);
		_histoPanel.getSelectPanelY().addPropertyChangeListener(this);
		
		addSouth();
		pack();
		DialogUtilities.centerDialog(this);
	}
	
	//add the buttons
	private void addSouth(){
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 200, 10));
		
		_okButton = new JButton("  OK  ");
		_okButton.setEnabled(false);
		_cancelButton = new JButton("Cancel");
		
		_okButton.addActionListener(this);
		_cancelButton.addActionListener(this);
		
		sp.add(_okButton);
		sp.add(_cancelButton);
		add(sp, BorderLayout.SOUTH);
	}
	
	/**
	 * Get the reason the dialog closed
	 * @return the reason the dialog closed
	 */
	public int getReason() {
		return _reason;
	}
	
	/**
	 * Return a HistoData ready for filling if the user hit ok
	 * @return a HistoData or <code>null</code>.
	 */
	public Histo2DData getHistoData() {
		if (_reason == DialogUtilities.OK_RESPONSE) {
			return _histoPanel.getHisto2DData();
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _okButton) {
			_reason = DialogUtilities.OK_RESPONSE;
			setVisible(false);
		}
		else if (o == _cancelButton) {
			_reason = DialogUtilities.CANCEL_RESPONSE;
			setVisible(false);
		}
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		String prop = evt.getPropertyName();
		
		if ((o == _histoPanel.getSelectPanelX()) && prop.equals("newname")) {
			_xName = (String)(evt.getNewValue());
		}	
		else if ((o == _histoPanel.getSelectPanelY()) && prop.equals("newname")) {
			_yName = (String)(evt.getNewValue());
		}	
		
		boolean xValid = ((_xName != null) && (_xName.length() > 4) && _xName.contains(":") && _xName.contains("."));
		boolean yValid = ((_yName != null) && (_yName.length() > 4) && _yName.contains(":") && _yName.contains("."));
		_okButton.setEnabled(xValid && yValid);
	}
	
	/**
	 * Get the name for the x axis variable
	 * @return the name for the x axis variable
	 */
	public String getXName() {
		return _xName;
	}

	
	/**
	 * Get the name for the y axis variable
	 * @return the name for the y axis variable
	 */
	public String getYName() {
		return _yName;
	}

	public static void main(String arg[]) {
		DefineHisto2DDialog dialog = new DefineHisto2DDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			System.err.println("OK");
		}
		else {
			System.err.println("CANCEL");
		}
		
		System.exit(0);
	}

}