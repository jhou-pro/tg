package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAmountType;

/**
 * This is a test entity, which is currently used for testing of db operations on tax sensitive {@link Money} instances.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("SIMPLE_TAX_MONEY_CLASS_TABLE")
public class EntityWithSimpleTaxMoney extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo("MONEY") @PersistedType(userType = ISimplyMoneyWithTaxAmountType.class)
    private Money money;

    protected EntityWithSimpleTaxMoney() {
    }

    public EntityWithSimpleTaxMoney(final String key, final String desc, final Money money) {
	super(null, key, desc);
	setMoney(money);
    }

    @Observable
    public void setMoney(final Money money) {
	if (money == null) {
	    throw new IllegalArgumentException("money should not be null");
	}
	if (money.getTaxAmount() == null) {
	    throw new IllegalArgumentException("money should tax sensitive");
	}
	this.money = money;
    }

    public Money getMoney() {
	return money;
    }
}
