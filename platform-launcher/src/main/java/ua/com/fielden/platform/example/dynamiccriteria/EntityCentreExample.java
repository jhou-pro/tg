package ua.com.fielden.platform.example.dynamiccriteria;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.util.Properties;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.client.ui.DefaultApplicationMainPanel;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.Rdbms;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.master.SimpleCompositeEntityMasterFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.UndockableTreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

public class EntityCentreExample extends AbstractUiApplication {

    private EntityFactory entityFactory;
    private ICriteriaGenerator criteriaGenerator;
    private IEntityMasterManager masterManager;
    private IGlobalDomainTreeManager gdtm;
    private EntityMasterManager emm;
    private IUserProvider userProvider;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	//TODO please review entity_centre_example.properties and correct appropriate properties.
	final String configFileName = args.length == 1 ? args[0] : "src/main/resources/entity_centre_example.properties";
	final FileInputStream in = new FileInputStream(configFileName);
	final Properties props = IDomainDrivenTestCaseConfiguration.hbc;
	props.load(in);
	in.close();

	// override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
	props.put("hibernate.show_sql", "false");
	props.put("hibernate.format_sql", "true");
	//	props.put("hibernate.hbm2ddl.auto", "create");

	final EntityCentreDataPopulationConfiguration config = new EntityCentreDataPopulationConfiguration();

	//	final PopulateDbForEntityCentreExample popDb = new PopulateDbForEntityCentreExample(config);
	//	popDb.createAndPopulate();

	configEntityMasterManager(config.getInjector());

	entityFactory = config.getEntityFactory();
	masterManager = config.getInstance(IEntityMasterManager.class);
	criteriaGenerator = config.getInstance(ICriteriaGenerator.class);
	gdtm = config.getInstance(IGlobalDomainTreeManager.class);
	emm = EntityCentreExampleMasterManagerConfig.createEntityMasterFactory(config.getInjector());
	userProvider = config.getInstance(IUserProvider.class);

	super.beforeUiExposure(args, splashController);
    }

    //    private void configValidation(final DomainValidationConfig dvc){
    //		dvc.setValidator(ExpressionEntity.class, "expression", new ExpressionValidator());
    //    }

    private static void configEntityMasterManager(final Injector injector){
	final EntityMasterManager entityMasterManager = (EntityMasterManager) injector.getInstance(IEntityMasterManager.class);
	entityMasterManager.addFactory(SimpleCompositeEntity.class, injector.getInstance(SimpleCompositeEntityMasterFactory.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {

	final TreeMenuItem<?> menuItems = new TreeMenuItem("root", "root panel");
	final BaseFrame mainApplicationFrame = new BaseFrame("Expression editor example", emm.getEntityMasterCache());
	final UndockableTreeMenuWithTabs<?> menu = new UndockableTreeMenuWithTabs(menuItems, new WordFilter(), userProvider, new BlockingIndefiniteProgressPane(mainApplicationFrame));

	//Configuring menu
	menuItems.addItem(new MiWithConfigurationSupport<SimpleECEEntity>("Simple entity", "Simple entity description",//
		menu,new StubMenuItemVisibilityProvider(),SimpleECEEntity.class,null,gdtm,entityFactory,masterManager,criteriaGenerator));
	menuItems.addItem(new MiWithConfigurationSupport<SimpleCompositeEntity>("Simple composite entity", //
		"Simple composite entity description", menu, new StubMenuItemVisibilityProvider(),//
		SimpleCompositeEntity.class,null,gdtm,entityFactory,masterManager,criteriaGenerator));
	menu.getModel().getOriginModel().reload();


	mainApplicationFrame.setPreferredSize(new Dimension(1280, 800));
	mainApplicationFrame.add(new DefaultApplicationMainPanel(menu));
	mainApplicationFrame.pack();

	RefineryUtilities.centerFrameOnScreen(mainApplicationFrame);
	SimpleLauncher.show("Expression editor example", mainApplicationFrame, null);
    }

    public static void main(final String[] args) {
	Rdbms.rdbms = Rdbms.H2;
	new EntityCentreExample().launch(args);
    }

}
