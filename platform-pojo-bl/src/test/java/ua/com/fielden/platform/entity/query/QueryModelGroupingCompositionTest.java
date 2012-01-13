package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.GroupModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.model.elements.YearOfModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelGroupingCompositionTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test_query_with_one_group() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).groupBy().prop("lastWo").yield().prop("lastWo").modelAsEntity(TgWorkOrder.class);
	final EntQuery act = qb.generateEntQuery(qry);

	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(new EntProp("lastWo"), "id"));
	final YieldsModel exp = new YieldsModel(yields);

	assertEquals("models are different", exp, act.getYields());

	final List<GroupModel> groups = new ArrayList<GroupModel>();
	groups.add(new GroupModel(new EntProp("lastWo")));
	final GroupsModel exp2 = new GroupsModel(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_with_several_groups() {
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).groupBy().prop("eqClass").groupBy().yearOf().prop("initDate").yield().prop("eqClass").as("eqClass").yield().yearOf().prop("initDate").as("initYear").modelAsAggregate();
	final EntQuery act = qb.generateEntQuery(qry);
	final YearOfModel yearOfModel = new YearOfModel(new EntProp("initDate"));
	final EntProp eqClassProp = new EntProp("eqClass");

	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(eqClassProp, "eqClass"));
	yields.add(new YieldModel(yearOfModel, "initYear"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, act.getYields());

	final List<GroupModel> groups = new ArrayList<GroupModel>();
	groups.add(new GroupModel(eqClassProp));
	groups.add(new GroupModel(yearOfModel));
	final GroupsModel exp2 = new GroupsModel(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }
}