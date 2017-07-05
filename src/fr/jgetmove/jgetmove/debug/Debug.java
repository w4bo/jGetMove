package fr.jgetmove.jgetmove.debug;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * Utilisée afin d'afficher les erreurs
 */
public final class Debug {
    public final static short DEBUG = -1;
    public final static short INFO = 0;
    public final static short WARNING = 1;
    public final static short ERROR = 2;

    private final static String DEBUG_STRING = "\033[0;32m[DEBUG]\033[0m ";
    private final static String INFO_STRING = "\033[0;34m[INFO ]\033[0m ";
    private final static String WARNING_STRING = "\033[0;33m[WARNG] ";//\033[0m ";
    private final static String ERROR_STRING = "\033[0;31m[ERROR] ";//"\033[0m ";

    private final static String DEFAULT_SEPARATOR = "|";
    private final static String MORE = "\\";
    private final static String LESS = "/";
    private final static String METHOD_SEPARATOR = "+";
    private final static String METHOD_PREFIX = "--:";
    private final static String METHOD_SUFFIX = ":--";
    private final static String METHOD_FULL_DISPLAY_SEPARATOR = "·";
    private final static String VARNAME_SEPARATOR = ": ";

    private static boolean displayDebug = false;
    private static String severity = "";
    private static String path = "";
    private static String content = "";
    private static ArrayList<Integer> customStackPositions = new ArrayList<>();
    private static String lastMethodName = "";
    private static int lastPathSize = 0;
    private static int sizeDirection = 0;

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     * @deprecated use {@link #println(Object, short)}
     */
    public static void println(Object o) {
        if (displayDebug) {
            severity = getSeverityString(DEBUG);
            updateDebugString();
            System.out.println(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(Object o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.print(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(PrettyPrint o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.print(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void println(Object o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.println(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void println(PrettyPrint o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.println(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(name + Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(String name, Object o, short status) {
        if (displayDebug) {
            severity = getSeverityString(DEBUG);
            updateDebugString();
            System.out.print(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(name + Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(String name, PrettyPrint o, short status) {
        if (displayDebug) {
            severity = getSeverityString(DEBUG);
            updateDebugString();
            System.out.print(concatAll(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(name + Object)
     * </pre>
     *
     * @param name le nom de la variable
     * @param o    l'objet a afficher
     */
    public static void println(String name, Object o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.println(concatAll(name, o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(name + Object)
     * </pre>
     *
     * @param name le nom de la variable
     * @param o    l'objet a afficher
     */
    public static void println(String name, PrettyPrint o, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.println(concatAll(name, o));
        }
    }

    public static void printTitle(String title, short status) {
        if (displayDebug || status > DEBUG) {
            severity = getSeverityString(status);
            updateDebugString();
            System.out.println(concatAll(createTitle(title)));
        }
    }

    public static String now() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        return df.format(new Date()) + "," + System.currentTimeMillis() % 1000;
    }

    /**
     * Active l'affichage du debug
     */
    public static void enable() {
        displayDebug = true;
    }

    /**
     * Ajoute un path personalisé
     *
     * @param letter l'initiale a afficher dans la traceroute
     */
    public static void stack(char letter) {
        stack(letter, null, 3);
    }

    /**
     * Ajoute un stack personalisé
     *
     * @param letter l'initiale a afficher dans la traceroute
     * @param title  Le titre à afficher lors de l'entree dans le path
     */
    public static void stack(char letter, String title) {
        stack(letter, title, 3);
    }

    /**
     * Affiche le nom de la methode courante.
     */
    public static void displayTitle() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];
        content = "";
        updatePath(stackTrace, 2);

        String methodName = getMethodName(stackTraceElement, true);

        if (methodName != null) {
            System.out.println(concatAll(createTitle(methodName)));
            updateLastMethodName(stackTraceElement.getMethodName());
        }
    }

    /**
     *
     */
    public static void unstack() {
        if (customStackPositions.size() > 0) {
            customStackPositions.remove(customStackPositions.size() - 1);
        }
    }


    public static String indent(String multiline) {
        return multiline.replaceAll("(?m)(\r?\n)", "$1\t");
    }

    private static String dateBlock() {
        return "\033[0;37m[" + now() + "]\033[0m";
    }

    private static String getSeverityString(short severity) {
        switch (severity) {
            case DEBUG:
                return DEBUG_STRING;
            case WARNING:
                return WARNING_STRING;
            case ERROR:
                return ERROR_STRING;
            default:
            case INFO:
                return INFO_STRING;
        }
    }

    /**
     * Ajoute un stack personalisé
     *
     * @param letter  l'initiale a afficher dans la traceroute
     * @param title   Le titre à afficher lors de l'entree dans le path
     * @param padding le padding pour la gestion du path
     */
    private static void stack(char letter, String title, int padding) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[padding];
        content = "";
        int originalPathSize = lastPathSize;

        updatePath(stackTrace, padding);

        if (!lastMethodName.equals(stackTraceElement.getMethodName())) {
            String methodName = getMethodName(stackTraceElement);

            if (methodName != null) {
                System.out.println(concatAll(createTitle(methodName)));
                originalPathSize = lastPathSize;
            }

            updateLastMethodName(stackTraceElement.getMethodName());
        }

        customStackPositions.add(path.length());
        path += letter;
        sizeDirection = Integer.compare(originalPathSize, path.length());
        lastPathSize = path.length();

        if (title != null) {
            System.out.println(concatAll(createTitle(title)));
        }

    }

    private static void updateDebugString() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[3];
        content = "";
        updatePath(stackTrace, 3);

        String methodName = getMethodName(stackTraceElement);

        if (methodName != null && !lastMethodName.equals(stackTraceElement.getMethodName())) {
            content = createTitle(methodName).concat(System.getProperty("line.separator"));
            sizeDirection = 0;
            updateLastMethodName(stackTraceElement.getMethodName());
        }

        if (path.length() > 0) {
            if (content.length() > 0) {
                content = content.concat(dateBlock()).concat(severity).concat(path);
            }
            content = content.concat(" ").concat(pathLine(sizeDirection, DEFAULT_SEPARATOR)).concat(" ");
        }

    }

    private static void updatePath(StackTraceElement[] stackTrace, int padding) {
        path = createPath(stackTrace, padding);
        sizeDirection = Integer.compare(lastPathSize, path.length());
        lastPathSize = path.length();
    }

    /**
     * Updates {@link #lastMethodName} only if methodName is different
     *
     * @param methodName the name of the actual method
     */
    private static void updateLastMethodName(String methodName) {
        if (!lastMethodName.equals(methodName)) {
            lastMethodName = methodName;
        }
    }

    private static String pathLine(int sizeDirection, String defaultSeparator) {
        if (sizeDirection == 0) {
            return defaultSeparator;
        } else if (sizeDirection < 0) {
            return MORE;
        } else {
            return LESS;
        }
    }

    private static String createPath(StackTraceElement[] stackTrace, int padding) {
        StringBuilder str = new StringBuilder();
        for (int i = stackTrace.length - 1; i >= padding; i--) {
            TraceMethod traceMethod =
                    getTraceMethod(stackTrace[i].getClassName(), stackTrace[i].getMethodName());

            if (traceMethod != null) {
                if (i == padding && traceMethod.displayTitleIfLast()) {
                    // dernier element du path et on souhaite afficher le nom de la methode
                    // on ajoute le separateur
                    str.append(METHOD_FULL_DISPLAY_SEPARATOR)
                            .append(getAvaliableMethodName(stackTrace[i], traceMethod));
                } else {
                    // si ce n'est pas le dernier element, ou si celui-ci n'est pas affiché en entier
                    str.append(getMethodSymbol(stackTrace[i], traceMethod));
                }
            }

            while (customStackPositions.contains(str.length())) {
                str.append(path.charAt(str.length()));
            }
        }
        return str.toString();
    }

    private static TraceMethod getTraceMethod(String className, String methodName) {
        try {
            for (Method method : Class.forName(className).getDeclaredMethods()) {
                if (Objects.equals(method.getName(), methodName) && method.isAnnotationPresent(TraceMethod.class)) {
                    return method.getAnnotation(TraceMethod.class);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static char getMethodSymbol(StackTraceElement stackTraceElement, TraceMethod traceMethod) {
        if (traceMethod.symbol() != 0) {
            return traceMethod.symbol();
        }
        return stackTraceElement.getMethodName().charAt(0);
    }

    /**
     * Renvoi le nom de la methode, ou celui spécifié par l'annotation {@link TraceMethod}
     *
     * @param stackTraceElement l'element à partir duquel on récupère la méthode
     * @return le nom de la méthode ou le titre qui à été spécifié dans l'annotation {@link TraceMethod}, null si {@link TraceMethod#displayTitle()} est a false
     */
    private static String getMethodName(StackTraceElement stackTraceElement) {
        return getMethodName(stackTraceElement, false);
    }

    /**
     * Renvoi le nom de la methode, ou celui spécifié par l'annotation {@link TraceMethod} formatté en tant que titre
     *
     * @param stackTraceElement l'element à partir duquel on récupère la méthode
     * @param forceDisplay      Surpasse la permission de l'annotation {@link TraceMethod#displayTitle()} et affiche son nom
     * @return le nom de la méthode ou le titre qui à été spécifié dans l'annotation {@link TraceMethod}, null si la methode n'est pas trouvée.
     */
    private static String getMethodName(StackTraceElement stackTraceElement, boolean forceDisplay) {
        TraceMethod traceMethod = getTraceMethod(stackTraceElement.getClassName(), stackTraceElement.getMethodName());

        if (traceMethod != null && (traceMethod.displayTitle() || forceDisplay)) {
            return getAvaliableMethodName(stackTraceElement, traceMethod);
        }
        return null;
    }

    private static String getAvaliableMethodName(StackTraceElement stackTraceElement, TraceMethod traceMethod) {
        // sinon on affiche le nom de la fonction
        if (traceMethod.title().isEmpty()) {
            // si la fonction à un titre personalisé
            return stackTraceElement.getMethodName();
        }
        return traceMethod.title();
    }

    /**
     * @param title le texte a transformer en titre
     * @return le titre sera prefixé et suffixé par {@link #METHOD_PREFIX} et {@link #METHOD_SUFFIX}
     */
    private static String createTitle(String title) {
        return " ".concat(pathLine(sizeDirection, METHOD_SEPARATOR)).concat(METHOD_PREFIX)
                .concat(" \033[4m").concat(title).concat("\033[0m ")
                .concat(METHOD_SUFFIX).concat(pathLine(sizeDirection, METHOD_SEPARATOR));
    }

    private static String concatAll(Object o) {
        return concatAll(o.toString());
    }

    private static String concatAll(PrettyPrint o) {
        return concatAll(o.toPrettyString());
    }

    private static String concatAll(String str) {
        return dateBlock().concat(severity).concat(path).concat(content).concat(str);
    }

    private static String concatAll(String name, Object o) {
        return concatAll(name, o.toString());
    }

    private static String concatAll(String name, PrettyPrint o) {
        return concatAll(name, o.toPrettyString());
    }

    private static String concatAll(String name, String value) {
        return concatAll(name.concat(VARNAME_SEPARATOR).concat(value));
    }
}
