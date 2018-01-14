package com.matthewtamlin.shamir.corecrypto.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.sun.istack.internal.NotNull;

/**
 * Gson TypeAdapterFactory for all classes in the {@link com.matthewtamlin.shamir.corecrypto.model} package.
 */
public class ModelTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> TypeAdapter<T> create(@NotNull final Gson gson, @NotNull final TypeToken<T> type) {
        final Class<T> rawType = (Class<T>) type.getRawType();
        
        if (CreationScheme.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) CreationScheme.typeAdapter(gson);
            
        } else if (RecoveryScheme.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) RecoveryScheme.typeAdapter(gson);
            
        } else if (Share.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) Share.typeAdapter(gson);
            
        } else {
            return null;
        }
    }
}