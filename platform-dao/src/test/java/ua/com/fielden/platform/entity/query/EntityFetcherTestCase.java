package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

import static org.junit.Assert.assertEquals;



public class EntityFetcherTestCase extends BaseEntQueryTCase {
    final Injector injector = new ApplicationInjectorFactory().add(new HibernateUserTypesModule()).getInjector();
    private EntityResultTreeBuilder ef = new EntityResultTreeBuilder(new MappingsGenerator(new HashMap<Class, Class>(), injector, new ArrayList<Class<? extends AbstractEntity>>()));
    private EntityFetcher entFetcher = new EntityFetcher(null, null, null, null, null, null) {};

    @Test
    @Ignore
    public void test_entity_tree_for_plain_entity() throws Exception {
	final List<PropertyPersistenceInfo> propInfos = new ArrayList<PropertyPersistenceInfo>();
//	propInfos.add(new ResultPropertyInfo("id", "C1", LONG));
//	propInfos.add(new ResultPropertyInfo("version", "C2", LONG));
//	propInfos.add(new ResultPropertyInfo("key", "C3", STRING));
//	propInfos.add(new ResultPropertyInfo("desc", "C4", STRING));
//	propInfos.add(new ResultPropertyInfo("initDate", "C5", DATE));

	final EntityTree expEntTree = new EntityTree(TgVehicle.class);
//	expEntTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 0);
//	expEntTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 1);
//	expEntTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 2);
//	expEntTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 3);
//	expEntTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 4);

	final EntityTree actTree = ef.buildEntityTree(TgVehicle.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);

	final List<Pair<String, Type>> expScalarInfo = new ArrayList<Pair<String, Type>>();
	expScalarInfo.add(new Pair<String, Type>("C4", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C1", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C5", Hibernate.TIMESTAMP));
	expScalarInfo.add(new Pair<String, Type>("C3", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C2", Hibernate.LONG));

	assertEquals("Act entity scalar info differs from expected", expScalarInfo, entFetcher.getScalarInfo(actTree));
    }

    @Test
    @Ignore
    public void test_entity_tree_for_plain_entity2() throws Exception {
	final List<PropertyPersistenceInfo> propInfos = new ArrayList<PropertyPersistenceInfo>();
//	propInfos.add(new ResultPropertyInfo("id", "C1", LONG));
//	propInfos.add(new ResultPropertyInfo("version", "C2", LONG));
//	propInfos.add(new ResultPropertyInfo("key", "C3", STRING));
//	propInfos.add(new ResultPropertyInfo("desc", "C4", STRING));
//	propInfos.add(new ResultPropertyInfo("initDate", "C5", DATE));
//	propInfos.add(new ResultPropertyInfo("model", "C6", MODEL));

	final EntityTree expEntTree = new EntityTree(TgVehicle.class);
//	expEntTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 0);
//	expEntTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 1);
//	expEntTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 2);
//	expEntTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 3);
//	expEntTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 4);

	final EntityTree<TgVehicleModel> expVehModelTree = new EntityTree<TgVehicleModel>(MODEL);
//	expVehModelTree.getSingles().put(new PropColumn("id", "C6", Hibernate.LONG, null), 5);

	expEntTree.getComposites().put("model", expVehModelTree);

	final EntityTree actTree = ef.buildEntityTree(TgVehicle.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);

	final List<Pair<String, Type>> expScalarInfo = new ArrayList<Pair<String, Type>>();
	expScalarInfo.add(new Pair<String, Type>("C4", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C1", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C5", Hibernate.TIMESTAMP));
	expScalarInfo.add(new Pair<String, Type>("C3", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C2", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C6", Hibernate.LONG));

	assertEquals("Act entity scalar info differs from expected", expScalarInfo, entFetcher.getScalarInfo(actTree));
    }

    @Test
    @Ignore
    public void test_entity_tree_for_plain_entity_with_custom_user_type_prop() throws Exception {
	final List<PropertyPersistenceInfo> propInfos = new ArrayList<PropertyPersistenceInfo>();
//	propInfos.add(new ResultPropertyInfo("id", "C1", LONG));
//	propInfos.add(new ResultPropertyInfo("version", "C2", LONG));
//	propInfos.add(new ResultPropertyInfo("key", "C3", STRING));
//	propInfos.add(new ResultPropertyInfo("desc", "C4", STRING));
//	propInfos.add(new ResultPropertyInfo("purchasePrice.amount", "C5", BIG_DECIMAL));

	final ValueTree expMoneyTree = new ValueTree(SimpleMoneyType.class.newInstance());
//	expMoneyTree.getSingles().put(new PropColumn("amount", "C5", Hibernate.BIG_DECIMAL, null), 4);
	final EntityTree expEntTree = new EntityTree(TgVehicle.class);
//	expEntTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 0);
//	expEntTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 1);
//	expEntTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 2);
//	expEntTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 3);
	expEntTree.getCompositeValues().put("purchasePrice", expMoneyTree);

	final EntityTree actTree = ef.buildEntityTree(TgVehicle.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);

	final List<Pair<String, Type>> expScalarInfo = new ArrayList<Pair<String, Type>>();
	expScalarInfo.add(new Pair<String, Type>("C4", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C1", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C3", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C2", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C5", Hibernate.BIG_DECIMAL));

	assertEquals("Act entity scalar info differs from expected", expScalarInfo, entFetcher.getScalarInfo(actTree));
    }

    @Test
    @Ignore
    public void test_entity_tree_for_entity_with_entity_property() throws Exception {
	final List<PropertyPersistenceInfo> propInfos = new ArrayList<PropertyPersistenceInfo>();
//	propInfos.add(new ResultPropertyInfo("vehicle.id", "C1", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.version", "C2", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.key", "C3", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.desc", "C4", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.initDate", "C5", DATE));
//	propInfos.add(new ResultPropertyInfo("id", "C6", LONG));
//	propInfos.add(new ResultPropertyInfo("version", "C7", LONG));
//	propInfos.add(new ResultPropertyInfo("key", "C8", STRING));
//	propInfos.add(new ResultPropertyInfo("desc", "C9", STRING));

	final EntityTree expVehTree = new EntityTree(TgVehicle.class);
//	expVehTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 4);
//	expVehTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 5);
//	expVehTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 6);
//	expVehTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 7);
//	expVehTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 8);

	final EntityTree expEntTree = new EntityTree(TgWorkOrder.class);
//	expEntTree.getSingles().put(new PropColumn("id", "C6", Hibernate.LONG, null), 0);
//	expEntTree.getSingles().put(new PropColumn("version", "C7", Hibernate.LONG, null), 1);
//	expEntTree.getSingles().put(new PropColumn("key", "C8", Hibernate.STRING, null), 2);
//	expEntTree.getSingles().put(new PropColumn("desc", "C9", Hibernate.STRING, null), 3);
	expEntTree.getComposites().put("vehicle", expVehTree);
	final EntityTree actTree = ef.buildEntityTree(TgWorkOrder.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);
    }

    @Test
    @Ignore
    public void test_entity_tree_for_entity_with_nested_entity_properties() throws Exception {
	final List<PropertyPersistenceInfo> propInfos = new ArrayList<PropertyPersistenceInfo>();
//	propInfos.add(new ResultPropertyInfo("vehicle.id", "C1", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.version", "C2", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.key", "C3", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.desc", "C4", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.initDate", "C5", DATE));
//	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.id", "C6", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.version", "C7", LONG));
//	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.key", "C8", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.desc", "C9", STRING));
//	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.initDate", "C10", DATE));

//	propInfos.add(new ResultPropertyInfo("id", "C11", LONG));
//	propInfos.add(new ResultPropertyInfo("version", "C12", LONG));
//	propInfos.add(new ResultPropertyInfo("key", "C13",  STRING));
//	propInfos.add(new ResultPropertyInfo("desc", "C14", STRING));

	final EntityTree expReplacedByVehTree = new EntityTree(TgVehicle.class);
//	expReplacedByVehTree.getSingles().put(new PropColumn("id", "C6", Hibernate.LONG, null), 9);
//	expReplacedByVehTree.getSingles().put(new PropColumn("version", "C7", Hibernate.LONG, null), 10);
//	expReplacedByVehTree.getSingles().put(new PropColumn("key", "C8", Hibernate.STRING, null), 11);
//	expReplacedByVehTree.getSingles().put(new PropColumn("desc", "C9", Hibernate.STRING, null), 12);
//	expReplacedByVehTree.getSingles().put(new PropColumn("initDate", "C10", Hibernate.TIMESTAMP, null), 13);

	final EntityTree expVehTree = new EntityTree(TgVehicle.class);
//	expVehTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 4);
//	expVehTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 5);
//	expVehTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 6);
//	expVehTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 7);
//	expVehTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 8);
	expVehTree.getComposites().put("replacedBy", expReplacedByVehTree);

	final EntityTree expEntTree = new EntityTree(TgWorkOrder.class);
//	expEntTree.getSingles().put(new PropColumn("id", "C11", Hibernate.LONG, null), 0);
//	expEntTree.getSingles().put(new PropColumn("version", "C12", Hibernate.LONG, null), 1);
//	expEntTree.getSingles().put(new PropColumn("key", "C13", Hibernate.STRING, null), 2);
//	expEntTree.getSingles().put(new PropColumn("desc", "C14", Hibernate.STRING, null), 3);
	expEntTree.getComposites().put("vehicle", expVehTree);
	final EntityTree actTree = ef.buildEntityTree(TgWorkOrder.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);
    }
}