package eello.elpring.di.util;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericTypeResolver {

    private GenericTypeResolver() {
        // 인스턴스화 방지
        throw new IllegalStateException("Utility class");
    }

    /**
     * 파라미터의 제네릭 컴포넌트 타입을 안전하게 추출합니다.
     * 제네릭이 없거나 추적 실패 시 기본값으로 null을 반환합니다.
     */
    public static Class<?> getGenericComponentType(Parameter parameter) {
        Type[] actualTypes = getActualTypeArguments(parameter);
        if (actualTypes != null && actualTypes.length > 0) {
            Type actualType = actualTypes[0];
            if (actualType instanceof Class) {
                return (Class<?>) actualType;
            } else throw new IllegalStateException("actual type '" + actualType + "' is not a class");
        }
        return Object.class;
    }

    public static Class<?> getGenericKeyType(Parameter parameter) {
        Type[] actualTypes = getActualTypeArguments(parameter);
        if (actualTypes != null && actualTypes.length >= 2) {
            Type actualKeyType = actualTypes[0];
            if (actualTypes[0] instanceof Class) {
                return (Class<?>) actualKeyType;
            } else throw new IllegalStateException("actual type '" + actualKeyType + "' is not a class");
        }
        return null;
    }

    public static Class<?> getGenericValueType(Parameter parameter) {
        Type[] actualTypes = getActualTypeArguments(parameter);
        if (actualTypes != null && actualTypes.length >= 2) {
            Type actualValueType = actualTypes[1];
            if (actualTypes[1] instanceof Class) {
                return (Class<?>) actualValueType;
            } else throw new IllegalStateException("actual type '" + actualValueType + "' is not a class");
        }
        return null;
    }

    public static Type[] getActualTypeArguments(Parameter parameter) {
        Type genericType = parameter.getParameterizedType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return parameterizedType.getActualTypeArguments();
        }

        return null;
    }
}
