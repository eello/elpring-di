package eello.elpring.di.inbox;

import java.lang.reflect.*;

public class ResolvableType {

    private final Type type;
    private final Class<?> rawClass;

    private ResolvableType(Type type) {
        this.type = type;
        this.rawClass = resolveRawClass(type);
    }

    public static ResolvableType forClass(Class<?> clazz) {
        return new ResolvableType(clazz);
    }

    public static ResolvableType forType(Type type) {
        return new ResolvableType(type);
    }

    public boolean isArray() {
        return rawClass.isArray();
    }

    /**
     * 제네릭 중첩 꺼내기
     * ex) List<HttpMessageConverter<?>> 에서 0번째를 꺼내면 HttpMessageConverter<?> 리턴
     */
    public ResolvableType getGeneric(int index) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (index < actualTypeArguments.length) {
                return new ResolvableType(actualTypeArguments[index]);
            }
        }

        // 제네릭 인자가 없거나 인덱스를 벗어나면 와일드카드와 다름없는 Object 클래스로 치환
        return new ResolvableType(Object.class);
    }

    public ResolvableType getComponentType() {
        if (type instanceof GenericArrayType genericArrayType) {
            return new ResolvableType(genericArrayType.getGenericComponentType());
        }

        if (rawClass.isArray()) {
            return new ResolvableType(rawClass.getComponentType());
        }

        return new ResolvableType(Object.class);
    }

    public boolean isAssignableFrom(ResolvableType other) {
        if (other == null) {
            return false;
        }

        // 최외곽 껍데기 클래스 수준에서 상속/구현 관계가 성립하지 않으면 아예 탈락
        if (!rawClass.isAssignableFrom(other.rawClass)) {
            return false;
        }

        // 만약 한쪽이 와일드카드(?) 구조라면 제네릭 알맹이 상세 비교를 패스하고 호환 인정
        if (type instanceof WildcardType || other.type instanceof WildcardType) {
            return true;
        }

        // 둘 다 제네릭이 중첩된 ParameterizedType 이라면, 내부 알맹이까지 파고들어가 대조 (재귀 호출)
        if (type instanceof ParameterizedType && other.type instanceof ParameterizedType) {
            return getGeneric(0).isAssignableFrom(other.getGeneric(0));
        }

        return true;
    }

    public Class<?> toClass() {
        return rawClass;
    }

    /**
     * 자바의 다양한 Type 인터페이스에서 순수 Class 객체만 정교하게 추출하는 헬퍼 메서드
     */
    private Class<?> resolveRawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        } else if (type instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            return  (upperBounds.length > 0) ? resolveRawClass(upperBounds[0]) : Object.class;
        } else if (type instanceof GenericArrayType genericArrayType) {
            Class<?> componentClass = resolveRawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(componentClass, 0).getClass();
        }

        return Object.class;
    }

    public Type getType() {
        return type;
    }
}
