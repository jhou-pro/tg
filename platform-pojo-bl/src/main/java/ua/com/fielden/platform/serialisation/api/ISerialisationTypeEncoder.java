package ua.com.fielden.platform.serialisation.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

/**
 * The custom encoding / decoding contract that maps entity types to their string identifiers. Implementation of this contract is used mainly for 
 * generated by ASM types, which should be dependent on centre context, from which they were generated.
 * <p>
 * This contract is used for: ad-hoc registration of generated types on server's {@link TgJackson} serialisation engine, serialising type information along with 
 * actual entity instances, ...
 * 
 * @author TG Team
 *
 */
public interface ISerialisationTypeEncoder {
    /**
     * Encodes entity type to some custom form of {@link String} identifier which is used during {@link TgJackson} serialisation
     * of entity instances.
     * 
     * @param entityType
     * @return
     */
    <T extends AbstractEntity<?>> String encode(final Class<T> entityType);
    
    /**
     * Decodes {@link String} entity type identifier into entity type that is used during {@link TgJackson} deserialisation
     * of entity instances.
     * 
     * @param entityTypeId
     * @return
     */
    <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId);
    
    ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson);
}
