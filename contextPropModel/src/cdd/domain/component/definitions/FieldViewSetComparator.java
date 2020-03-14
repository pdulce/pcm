package cdd.domain.component.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cdd.common.utils.CommonUtils;
import cdd.domain.component.components.IViewComponent;


public class FieldViewSetComparator implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = 12138979320831L;

	private final String fieldToOrder, orderDir;

	public FieldViewSetComparator(final String fieldToOrder_, final String order_) {
		this.fieldToOrder = fieldToOrder_;
		this.orderDir = order_;
	}

	@Override
	public final int compare(final Object obj1, final Object obj2) {
		final FieldViewSet fieldViewSet1 = (FieldViewSet) obj1, fieldViewSet2 = (FieldViewSet) obj2;
		final int corrector = this.orderDir.equals(IViewComponent.ASC_LABEL) ? 1 : -1;
		try {
			final Serializable val1 = fieldViewSet1.getValue(this.fieldToOrder), val2 = fieldViewSet2.getValue(this.fieldToOrder);
			final IFieldView fieldView = fieldViewSet1.getFieldView(this.fieldToOrder);
			if (fieldView.getEntityField().getAbstractField().isNumeric()) {
				if (((Number) val1).longValue() < ((Number) val2).longValue()) {
					return -1 * corrector;
				} else if (((Number) val1).longValue() > ((Number) val2).longValue()) {
					return 1 * corrector;
				} else {
					return 0;
				}
			} else if (fieldView.getEntityField().getAbstractField().isDate()) {
				final Date date1 = (Date) val1, date2 = (Date) val2;
				if (date1.before(date2)) {
					return -1 * corrector;
				} else if (date1.after(date2)) {
					return 1 * corrector;
				} else {
					return 0;
				}
			} else if (fieldView.getEntityField().getAbstractField().isBoolean()) {
				final Integer bool1 = (Integer) val1, bool2 = (Integer) val2;
				if (bool1.compareTo(bool2) < 0) {
					return -1 * corrector;
				} else if (bool1.compareTo(bool2) > 0) {
					return 1 * corrector;
				} else {
					return 0;
				}
			} else if (fieldView.getEntityField().getAbstractField().isDecimal()) {
				if (((Number) val1).doubleValue() < ((Number) val2).doubleValue()) {
					return -1 * corrector;
				} else if (((Number) val1).doubleValue() > ((Number) val2).doubleValue()) {
					return 1 * corrector;
				} else {
					return 0;
				}
			} else if (!fieldView.getEntityField().getAbstractField().isBlob()) {// si es de texto
				// analizamos si es numorico val1
				if (CommonUtils.isNumeric((String) val1) && !CommonUtils.isNumeric((String) val2)) {
					return -1 * corrector;
				} else if (!CommonUtils.isNumeric((String) val1) && CommonUtils.isNumeric((String) val2)) {
					return 1 * corrector;
				} else if (CommonUtils.isNumeric((String) val1) && CommonUtils.isNumeric((String) val2)) {
					if (Long.parseLong(val1.toString()) < Long.parseLong(val2.toString())) {
						return -1 * corrector;
					} else if (Long.parseLong(val1.toString()) > Long.parseLong(val2.toString())) {
						return 1 * corrector;
					} else {
						return 0;
					}
				} else {
					final List<String> l = new ArrayList<String>();
					l.add((String) val1);
					l.add((String) val2);
					Collections.sort(l);
					if (l.iterator().next().equals(val1)) {
						return -1 * corrector;
					}
					return 1 * corrector;
				}
			} else {
				return 0;
			}
		}
		catch (final Throwable exc) {
			return -1;
		}
	}

}
