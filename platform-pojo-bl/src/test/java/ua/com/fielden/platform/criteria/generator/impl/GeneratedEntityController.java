package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

public class GeneratedEntityController<T extends AbstractEntity<?>> implements IGeneratedEntityController<T> {

    @Override
    public Class<T> getEntityType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setEntityType(final Class<T> type) {
	// TODO Auto-generated method stub

    }

    @Override
    public Class<? extends Comparable> getKeyType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final int pageCapacity, final List<byte[]> binaryTypes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity, final List<byte[]> binaryTypes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> qem, final int pageNo, final int pageCapacity, final List<byte[]> binaryTypes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity, final List<byte[]> binaryTypes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> qem, final List<byte[]> binaryTypes) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles, final List<byte[]> binaryTypes) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

}