package graphql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogKit {

    /**
     * Creates a logger with a name indicating that the content might not be privacy safe
     * eg it could contain user generated content or privacy information.
     *
     * @param clazz the class to make a logger for
     *
     * @return a new Logger
     */
    public static Logger getNotPrivacySafeLogger(Class clazz) {
        return LoggerFactory.getLogger(String.format("%s_NotPrivacySafe", clazz.getName()));
    }

}
