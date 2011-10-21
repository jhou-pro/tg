package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.reportquery.ChartModelChangedEvent;
import ua.com.fielden.platform.reportquery.ChartModelChangedListener;
import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;
import ua.com.fielden.platform.utils.ConverterFactory;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;

public abstract class CalculatedNumber {

    private final String caption;

    private final EventListenerList listenerList;

    private final Converter numberConverter;

    private Number number;

    public CalculatedNumber(final String caption, final ICategoryChartEntryModel chartModel, final CalculatedNumber... calculatedNumbers){
	this.caption = caption;
	this.number = 0;
	this.numberConverter = ConverterFactory.createNumberConverter();
	this.listenerList = new EventListenerList();

	if(chartModel != null){
	    chartModel.addChartModelChangedListener(createChartModelChangedListener());
	}

	for(final CalculatedNumber number : calculatedNumbers){
	    number.addPropertyChangeListener(createNumberPropertyChangeListener());
	}
    }

    public CalculatedNumber(final String caption, final CalculatedNumber... calculatedNumbers){
	this(caption, null, calculatedNumbers);
    }

    private PropertyChangeListener createNumberPropertyChangeListener() {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		setNumber(calculate());
	    }
	};
    }

    private ChartModelChangedListener createChartModelChangedListener() {
	return new ChartModelChangedListener() {

	    @Override
	    public void cahrtModelChanged(final ChartModelChangedEvent event) {
		setNumber(calculate());
	    }
	};
    }

    abstract protected Number calculate();

    public final void setNumber(final Number number) {
	final Number oldNumber = this.number;
	this.number = number;
	notifyNumberChanged(new PropertyChangeEvent(this, "number", oldNumber, number));
    }

    public Number getNumber() {
	return number;
    }

    @Override
    public String toString() {
	return "<html><font color = #175c9a><b>" + caption + ":</b></font> " + numberConverter.convertToString(number)+"</html>";
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
	listenerList.add(PropertyChangeListener.class, l);
    }

    public void removePropertyChangeListener(final PropertyChangeListener l){
	listenerList.remove(PropertyChangeListener.class, l);
    }

    private void notifyNumberChanged(final PropertyChangeEvent event){
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==PropertyChangeListener.class) {
		((PropertyChangeListener)listeners[i+1]).propertyChange(event);
	    }
	}

    }

}
