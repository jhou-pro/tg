package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeRepresentation.PivotAddToAggregationTickRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeRepresentation.PivotAddToDistributionTickRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementPropertiesMap;

/**
 * A domain tree manager for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IPivotDomainTreeManager {
    private static final long serialVersionUID = -4155274305648154329L;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new PivotDomainTreeRepresentation(serialiser, rootTypes),null, new PivotAddToDistributionTickManager(), new PivotAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected PivotDomainTreeManager(final ISerialiser serialiser, final IPivotDomainTreeRepresentation dtr, final Boolean visible, final PivotAddToDistributionTickManager firstTick, final PivotAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
	return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
	return (IPivotAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
	return (IPivotDomainTreeRepresentation) super.getRepresentation();
    }

    public static class PivotAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IPivotAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToDistributionTickManager() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    return PivotDomainTreeRepresentation.getWidthByDefault(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    PivotDomainTreeRepresentation.setWidthByDefault(tr().getDtr(), root, property, width, propertiesWidths);
	}

	@Override
	protected PivotAddToDistributionTickRepresentation tr() {
	    return (PivotAddToDistributionTickRepresentation) super.tr();
	}
    }

    public static class PivotAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IPivotAddToAggregationTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToAggregationTickManager() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    return PivotDomainTreeRepresentation.getWidthByDefault(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    PivotDomainTreeRepresentation.setWidthByDefault(tr().getDtr(), root, property, width, propertiesWidths);
	}

	@Override
	protected PivotAddToAggregationTickRepresentation tr() {
	    return (PivotAddToAggregationTickRepresentation) super.tr();
	}
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<PivotDomainTreeManager> {
	public PivotDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeManager read(final ByteBuffer buffer) {
	    final PivotDomainTreeRepresentation dtr = readValue(buffer, PivotDomainTreeRepresentation.class);
	    final PivotAddToDistributionTickManager firstTick = readValue(buffer, PivotAddToDistributionTickManager.class);
	    final PivotAddToAggregationTickManager secondTick = readValue(buffer, PivotAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    return new PivotDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick);
	}
    }
}
