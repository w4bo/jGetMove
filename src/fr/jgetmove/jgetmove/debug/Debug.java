package fr.jgetmove.jgetmove.debug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Utilisée afin d'afficher les erreurs
 */
public class Debug {
    private final static String SEPARATOR = " | ";
    private final static String SEPARATOR_MORE = " \\ ";
    private final static String SEPARATOR_LESS = " / ";
    private final static String METHOD_PREFIX = " +-- ";
    private final static String METHOD_PREFIX_MORE = " \\-∵ ";
    private final static String METHOD_PREFIX_LESS = "  /-∴ ";
    private final static String METHOD_SUFFIX = " ∵∴ " + System.getProperty("line.separator");
    private final static String STACK_TITLE_PREFIX = ":";
    private final static String METHOD_SEPARATOR = "·";

    private static boolean displayDebug = false;
    private static String stack = "";
    private static ArrayList<Integer> customStackPositions = new ArrayList<>();
    private static String lastMethodName = "";
    private static int lastStackLenght = 0;
    private static int sizeDirection = 0;
    ;

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(Object o) {
        if (displayDebug) {
            updateStack();
            System.out.print(prefixStack(o));
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
    public static void println(Object o) {
        if (displayDebug) {
            updateStack();
            System.out.println(prefixStack(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.err.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void err(Object o) {
        if (displayDebug) {
            updateStack();
            System.err.print(prefixStack(o));
        }
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.err.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void errln(Object o) {
        if (displayDebug) {
            updateStack();
            System.err.println(prefixStack(o));
        }
    }

    /**
     * Active l'affichage du debug
     */
    public static void enable() {
        displayDebug = true;
    }

    public static void stack(char letter) {
        updateStack();
        customStackPositions.add(stack.length());
        stack += letter;
    }

    /**
     * Ajoute un stack personalisé
     *
     * @param letter
     * @param title
     */
    public static void stack(char letter, String title) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];

        int originalLastStackLenght = lastStackLenght;

        addPathToStack(2, stackTrace);

        sizeDirection = Integer.compare(lastStackLenght, stack.length());
        lastStackLenght = stack.length();

        String methodTitle = getMethodTitle(stackTraceElement);

        customStackPositions.add(stack.length());
        stack += letter;
        sizeDirection = Integer.compare(lastStackLenght, stack.length());
        lastStackLenght = stack.length();

        if (methodTitle != null && !lastMethodName.equals(stackTraceElement.getMethodName())) {
            System.out.print(methodTitle);
        } else {
            sizeDirection = Integer.compare(originalLastStackLenght, stack.length());
        }


        if (title != null) {
            System.out.print(createTitle(title));
        }

        if (!lastMethodName.equals(stackTraceElement.getMethodName())) {
            lastMethodName = stackTraceElement.getMethodName();
        }
    }

    public static void unstack() {
        if (customStackPositions.size() > 0) {
            customStackPositions.remove(customStackPositions.size() - 1);
        }
    }

    public static void displayTitle() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int padding = 2;

        addPathToStack(padding, stackTrace);

        sizeDirection = Integer.compare(lastStackLenght, stack.length());
        lastStackLenght = stack.length();

        System.out.print(getMethodTitle(stackTrace[padding]));

        if (!lastMethodName.equals(stackTrace[padding].getMethodName())) {
            lastMethodName = stackTrace[padding].getMethodName();
        }

    }

    private static void updateStack() {
        //-updateStack() et - currentdebugfunction et -updateStack et à partir de 0
        updateStack(4);
    }

    private static void updateStack(int padding) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        addPathToStack(padding, stackTrace);

        sizeDirection = Integer.compare(lastStackLenght, stack.length());
        lastStackLenght = stack.length();

        addTitleToStack(stackTrace[padding]);

        if (stack.length() > 0) {
            stack = stack.concat(pathSeparatorDirection(sizeDirection, SEPARATOR, SEPARATOR_MORE, SEPARATOR_LESS));
        }

        if (!lastMethodName.equals(stackTrace[padding].getMethodName())) {
            lastMethodName = stackTrace[padding].getMethodName();
        }
    }

    private static String pathSeparatorDirection(int sizeDirection, String equal, String more, String less) {
        if (sizeDirection == 0) {
            return equal;
        } else if (sizeDirection < 0) {
            return more;
        } else {
            return less;
        }
    }

    private static void addPathToStack(int padding, StackTraceElement[] stackTrace) {
        StringBuilder str = new StringBuilder();
        for (int i = stackTrace.length - 1; i >= padding; i--) {
            TraceMethod traceMethod =
                    getTraceMethod(stackTrace[i].getClassName(), stackTrace[i].getMethodName());

            if (traceMethod != null) {
                if (i == padding && traceMethod.displayTitleIfLast()) {
                    // dernier element du stack et on souhaite afficher le nom de la methode
                    // on ajoute le separateur
                    str.append(METHOD_SEPARATOR).append(getAvaliableMethodTitle(stackTrace[i], traceMethod));
                } else {
                    // si ce n'est pas le dernier element, ou si celui-ci n'est pas affiché en entier
                    str.append(getMethodSymbol(stackTrace[i], traceMethod));
                }
            }

            while (customStackPositions.contains(str.length())) {
                str.append(stack.charAt(str.length()));
            }
        }
        stack = str.toString();
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

    private static void addTitleToStack(StackTraceElement stackTraceElement) {
        String methodTitle = getMethodTitle(stackTraceElement);

        if (methodTitle != null && !lastMethodName.equals(stackTraceElement.getMethodName())) {
            sizeDirection = Integer.compare(lastStackLenght, stack.length());
            lastStackLenght = stack.length();

            stack = methodTitle.concat(stack);
        }
    }

    /**
     * @param stackTraceElement l'element à partir duquel on récupère la méthode
     * @return le nom de la méthode ou le titre qui à été spécifié dans l'annotation {@link TraceMethod}
     */
    private static String getMethodTitle(StackTraceElement stackTraceElement) {
        TraceMethod traceMethod = getTraceMethod(stackTraceElement.getClassName(), stackTraceElement.getMethodName());

        if (traceMethod != null && traceMethod.displayTitle()) {
            return createTitle(getAvaliableMethodTitle(stackTraceElement, traceMethod));
        }
        return null;
    }

    private static String getAvaliableMethodTitle(StackTraceElement stackTraceElement, TraceMethod traceMethod) {
        // sinon on affiche le nom de la fonction
        if (Objects.equals(traceMethod.title(), "")) {
            // si la fonction à un titre personalisé
            return stackTraceElement.getMethodName();
        }
        return traceMethod.title();
    }

    private static String createTitle(String title) {
        return stack.concat(pathSeparatorDirection(sizeDirection, METHOD_PREFIX, METHOD_PREFIX_MORE, METHOD_PREFIX_LESS))
                .concat(title).concat(METHOD_SUFFIX);
    }

    private static String prefixStack(Object o) {
        return stack.concat(o.toString());
    }
}
