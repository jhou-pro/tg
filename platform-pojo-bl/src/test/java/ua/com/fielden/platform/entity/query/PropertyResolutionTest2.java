package ua.com.fielden.platform.entity.query;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyResolutionTest2 extends BaseEntQueryTCase {

    @Test
    public void test_prop_to_source_association1() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").model();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association2() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class). //
	join(TgVehicleModel.class).as("model").on().prop("model").eq().prop("model.id"). //
	where().prop("model.make.key").eq().val("MERC").model();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("model.id"), prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association3() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class). //
	join(TgVehicleModel.class).as("model").on().prop("model").eq().prop("model.id"). //
	join(TgVehicleMake.class).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("model.id"), prop("model.make") });
	final List<EntProp> qrySource3props = Arrays.asList(new EntProp[] { prop("model.make.id"), prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props, qrySource3props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association4() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v"). //
	join(TgVehicleModel.class).as("m").on().prop("model").eq().prop("m.id"). //
	where().prop("m.make.key").eq().val("MERC").and().prop("v.model.make.key").like().val("MERC%").model();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model"), prop("v.model.make.key") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("m.id"), prop("m.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association5() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").join(TgVehicleModel.class).as("m").on().prop("model").eq().prop("m").where().prop("m.make.key").eq().val("MERC").model();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("m"), prop("m.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association6() {
	final AggregatedResultQueryModel sourceQry = select(TgVehicle.class).groupBy().prop("model").yield().prop("model").as("model").yield().minOf().yearOf().prop("initDate").as("earliestInitYear").modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entQuery(qry);
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("model.make.key"), prop("earliestInitYear") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association7() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgVehicle.class). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("key.make.key").eq().val("MERC").and().prop("count").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entQuery(qry);
	entQry.validate();
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("key.make.key"), prop("count") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association8() {
	final AggregatedResultQueryModel sourceQry = select(TgVehicle.class). //
	groupBy().prop("model"). //
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();

	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(TgVehicle.class). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel qry = select(sourceQry).
	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
	where(). //
	prop("model.make.key").eq().val("MERC").and(). //
	prop("earliestInitYear").ge().val(2000).and(). //
	prop("count").ge().val(25).modelAsAggregate();

	final EntQuery entQry = entQuery(qry);
	entQry.validate();
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model"), prop("model.make.key"), prop("earliestInitYear") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("mc.key"), prop("count") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association9() {
	final AggregatedResultQueryModel sourceQry1 = select(TgVehicle.class). //
	groupBy().prop("model"). //
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();

	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(TgVehicle.class). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel sourceQry3 = select(sourceQry1).
	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
	where(). //
	prop("model.make.key").eq().val("MERC").and(). //
	prop("earliestInitYear").ge().val(2000).and(). //
	prop("count").ge().val(25). //
	groupBy().prop("model.make"). //
	yield().prop("model.make").as("make"). //
	yield().minOf().prop("earliestInitYear").as("earliestInitYearPerMake"). //
	yield().sumOf().prop("count").as("totalVehCount"). //
	modelAsAggregate();

	final AggregatedResultQueryModel qry = select(sourceQry3). //
	where().prop("totalVehCount").lt().val(100). //
	yield().prop("make.key").as("a1"). //
	yield().prop("earliestInitYearPerMake").as("a2"). //
	yield().prop("totalVehCount").as("a3"). //
	modelAsAggregate();

	final EntQuery entQry = entQuery(qry);
	entQry.validate();
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("totalVehCount"), prop("make.key"), prop("earliestInitYearPerMake"), prop("totalVehCount")});
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association10() {
	//select(OrgUnit5.class).where("parent.parent.parent.parent.key").eq().val("NORTH");
	final EntityResultQueryModel<TgOrgUnit5> qry = select(TgOrgUnit5.class). //
	join(TgOrgUnit4.class).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(TgOrgUnit3.class).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(TgOrgUnit2.class).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(TgOrgUnit1.class).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").model();
	final EntQuery entQry = entQuery(qry);
	entQry.validate();
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("parent") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("parent.id"), prop("parent.parent") });
	final List<EntProp> qrySource3props = Arrays.asList(new EntProp[] { prop("parent.parent.id"), prop("parent.parent.parent") });
	final List<EntProp> qrySource4props = Arrays.asList(new EntProp[] { prop("parent.parent.parent.id"), prop("parent.parent.parent.parent") });
	final List<EntProp> qrySource5props = Arrays.asList(new EntProp[] { prop("parent.parent.parent.parent.id"), prop("parent.parent.parent.parent.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props, qrySource3props, qrySource4props, qrySource5props}), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association11() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(TgOrgUnit5.class). //
	join(TgOrgUnit4.class).on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent").model();
	try {
	    entQuery(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_prop_to_source_association12() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(TgOrgUnit5.class). //
	join(TgOrgUnit4.class).on().prop("parent").eq().prop("parent"). //
	join(TgOrgUnit4.class).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(TgOrgUnit3.class).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(TgOrgUnit2.class).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(TgOrgUnit1.class).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").model();
	try {
	    entQuery(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }
}