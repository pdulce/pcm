package gedeoner.utils;

import java.lang.reflect.Method;
import java.util.Calendar;

public class MiClaseFuncion {
    
    public int functionToPass(String message) {
        return message.length()*(int)Calendar.getInstance().getTimeInMillis();
    }
    
    public void outerFunction(Object object, Method method, String message) throws Exception {
        Object[] parameters = new Object[1];
        parameters[0] = message;
        System.out.println(method.invoke(object, parameters));
    }

    public static void main(String[] args) throws Exception{
    	int a = 89;
    	long b = a;
    	/*
        Class[] parameterTypes = new Class[1];
        parameterTypes[0] = String.class;
        Method functionToPass = MiClaseFuncion.class.getMethod("functionToPass", parameterTypes[0]);
        MiClaseFuncion main = new MiClaseFuncion();
        main.outerFunction(main, functionToPass, "This is the input");*/
    	System.out.print(b);
    }
}