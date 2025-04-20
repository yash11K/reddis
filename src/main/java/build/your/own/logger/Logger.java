package build.your.own.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level {
        ERROR(1), WARN(2), INFO(3), DEBUG(4), TRACE(5);

        private final int priority;

        Level(int priority) {
            this.priority = priority;
        }
    }

    private static Logger instance;
    private final Level logLevel;
    private final String className;

    private Logger(Level logLevel, String className) {
        this.logLevel = logLevel;
        this.className = className;
    }

    public static synchronized Logger getInstance(Class<?> clazz) {
        if (instance == null) {
            instance = new Logger(Level.INFO, clazz.getSimpleName());
        }
        return instance;
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger(Level.INFO, "Main");
        }
        return instance;
    }

    public static synchronized void init(Level logLevel, Class<?> clazz) {
        if (instance == null) {
            instance = new Logger(logLevel, clazz.getSimpleName());
        }
    }

    private void log(Level level, String message) {
        if (level.priority <= this.logLevel.priority) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String formattedMessage = String.format("[%s] [%s] [%s] %s", timestamp, level, className, message);
            if (level == Level.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void warn(String message) {
        log(Level.WARN, message);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void trace(String message) {
        log(Level.TRACE, message);
    }
}
