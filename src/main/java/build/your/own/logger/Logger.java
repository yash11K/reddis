package build.your.own.logger;


import org.slf4j.LoggerFactory;

public class Logger {
    public Logger(org.slf4j.Logger internalLogger) {
        this.internalLogger = internalLogger;
    }

    private final org.slf4j.Logger internalLogger;


    public static Logger getInstance(Class<?> clazz){
        return new Logger(LoggerFactory.getLogger(clazz.getSimpleName()));
    }

    public void info(String msg) {
        internalLogger.info(msg);
    }

    public void debug(String msg) {
        internalLogger.debug(msg);
    }

    public void warn(String msg) {
        internalLogger.warn(msg);
    }

    public void error(String msg) {
        internalLogger.error(msg);
    }

    public void trace(String msg) {
        internalLogger.trace(msg);
    }
}
