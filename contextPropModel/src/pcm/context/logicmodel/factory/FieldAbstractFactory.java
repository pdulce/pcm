package pcm.context.logicmodel.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import pcm.context.logicmodel.definitions.IFieldAbstract;

public class FieldAbstractFactory {

	private static FieldAbstractFactory fieldAbstractFactory;

	private final Collection<IFieldAbstract> fieldFactory;

	public static int getCapacity() {
		return FieldAbstractFactory.getUniqueInstance().fieldFactory == null ? 0 : FieldAbstractFactory.getUniqueInstance().fieldFactory
				.size();
	}

	private FieldAbstractFactory() {
		this.fieldFactory = new ArrayList<IFieldAbstract>();
	}

	private static FieldAbstractFactory getUniqueInstance() {
		if (FieldAbstractFactory.fieldAbstractFactory == null) {
			FieldAbstractFactory.fieldAbstractFactory = new FieldAbstractFactory();
		}
		return FieldAbstractFactory.fieldAbstractFactory;
	}

	public static IFieldAbstract getFieldAbstract(final IFieldAbstract field) {
		IFieldAbstract fieldReturn = null;
		final IFieldAbstract fieldReceived = FieldAbstractFactory.searchInStaticMemory(field);
		if (fieldReceived == null) {
			FieldAbstractFactory.getUniqueInstance().fieldFactory.add(field);
			fieldReturn = field.copyOf();
		} else {
			fieldReturn = fieldReceived.copyOf();
		}
		return fieldReturn;
	}

	private static IFieldAbstract searchInStaticMemory(final IFieldAbstract field) {
		if (FieldAbstractFactory.getUniqueInstance().fieldFactory.isEmpty()) {
			return null;
		}
		final Iterator<IFieldAbstract> iteFields = FieldAbstractFactory.getUniqueInstance().fieldFactory.iterator();
		while (iteFields.hasNext()) {
			final IFieldAbstract field_ = iteFields.next();
			if (field_.isEquals(field)) {
				return field_;
			}
		}
		return null;
	}

}
