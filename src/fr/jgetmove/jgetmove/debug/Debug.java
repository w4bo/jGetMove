package fr.jgetmove.jgetmove.debug;

import java.util.ArrayList;

/**
 * Utilisée afin d'afficher les erreurs
 */
public class Debug {
    private final static String SEPARATOR = " ";
    private final static String METHOD_PREFIX = ".";
    private final static String STACK_TITLE_PREFIX = ":";

    private static boolean displayDebug = false;
    private static boolean displayTraces = false;
    private static boolean displayMethodNames = false;
    private static boolean displayMethodNameAtEnd = false;
    private static String stack = "";
    private static ArrayList<Integer> customStackPositions = new ArrayList<>();
    private static String lastMethodName = "";

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

    public static void enable() {
        enable(true, true, false);
    }

    public static void enable(boolean enableTraces, boolean enableMethodNames, boolean enableMethodNameAtEnd) {
        displayDebug = true;
        displayTraces = enableTraces;
        displayMethodNames = enableMethodNames;
        displayMethodNameAtEnd = enableMethodNameAtEnd;
    }

    public static void stack(char letter) {
        updateStack();
        customStackPositions.add(stack.length());
        stack += letter;
    }

    public static void stack(char letter, String title) {
        updateStack();
        customStackPositions.add(stack.length());
        stack += letter;

        if (title != null) {
            System.out.println(STACK_TITLE_PREFIX + title);
        }
    }

    public static void unstack() {
        if (customStackPositions.size() > 0) {
            customStackPositions.remove(customStackPositions.size() - 1);
        }
    }

    private static void updateStack() {
        //-updateStack() et - currentdebugfunction et -updateStack et à partir de 0
        updateStack(4);
    }

    private static void updateStack(int padding) {
        if (displayTraces) {
            StringBuilder str = new StringBuilder();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            for (int i = stackTrace.length - 1; i >= padding; i--) {
                if (i == padding && displayMethodNameAtEnd) {
                    str.append("/").append(stackTrace[i].getMethodName());
                } else {
                    str.append(stackTrace[i].getMethodName().charAt(0));
                }

                while (customStackPositions.contains(str.length())) {
                    str.append(stack.charAt(str.length()));
                }
            }

            stack = str.toString();

            if (displayMethodNames) {
                String methodName = stackTrace[padding].getMethodName();

                if (!lastMethodName.equals(methodName)) {
                    System.out.println(METHOD_PREFIX + methodName);
                    lastMethodName = methodName;
                }
            }
        }
    }

    private static String prefixStack(Object o) {
        return stack.concat(SEPARATOR).concat(o.toString());
    }
}
