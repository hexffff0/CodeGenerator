package me.lotabout.codegenerator.util;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.ElementComparator;
import org.jetbrains.java.generate.element.GenerationHelper;
import org.jetbrains.java.generate.exception.GenerateCodeException;
import org.jetbrains.java.generate.exception.PluginException;
import org.jetbrains.java.generate.velocity.VelocityFactory;
import org.mdkt.compiler.InMemoryJavaCompiler;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.google.common.collect.Maps;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.PathUtil;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.ext.ClassTemplate;

public class GenerationUtil {

    private static final Logger logger = Logger.getInstance("#" + GenerationUtil.class.getName());

    private static ClassLoader classLoader = GenerationUtil.class.getClassLoader();

    /**
     * Combines the two lists into one list of members.
     *
     * @param filteredFields fields to be included in the dialog
     * @param filteredMethods methods to be included in the dialog
     *
     * @return the combined list
     */
    public static PsiElementClassMember[] combineToClassMemberList(PsiField[] filteredFields, PsiMethod[] filteredMethods) {
        PsiElementClassMember[] members = new PsiElementClassMember[filteredFields.length + filteredMethods.length];

        // first add fields
        for (int i = 0; i < filteredFields.length; i++) {
            members[i] = new PsiFieldMember(filteredFields[i]);
        }

        // then add methods
        for (int i = 0; i < filteredMethods.length; i++) {
            members[filteredFields.length + i] = new PsiMethodMember(filteredMethods[i]);
        }

        return members;
    }

    public static List<PsiMember> convertClassMembersToPsiMembers(@Nullable List<PsiElementClassMember> classMemberList) {
        if (classMemberList == null || classMemberList.isEmpty()) {
            return Collections.emptyList();
        }
        List<PsiMember> psiMemberList = new ArrayList<>();

        for (PsiElementClassMember classMember : classMemberList) {
            psiMemberList.add(classMember.getElement());
        }

        return psiMemberList;
    }

    public static void insertMembersToContext(List<PsiMember> members, List<PsiMember> notNullMembers, Map<String, Object> context, String postfix, int sortElements) {
        logger.debug("insertMembersToContext - adding fields");
        // field information
        final List fieldElements = EntryUtils.getOnlyAsFieldEntries(members, notNullMembers, false);
        context.put("fields" + postfix, fieldElements);
        context.put("fields", fieldElements);
        if (fieldElements.size() == 1) {
            context.put("field" + postfix, fieldElements.get(0));
            context.put("field", fieldElements.get(0));
        }

        // method information
        logger.debug("insertMembersToContext - adding members");
        context.put("methods" + postfix, EntryUtils.getOnlyAsMethodEntrys(members));
        context.put("methods", EntryUtils.getOnlyAsMethodEntrys(members));

        // element information (both fields and methods)
        logger.debug("Velocity Context - adding members (fields and methods)");
        List<MemberEntry> elements = EntryUtils.getOnlyAsFieldAndMethodElements(members, notNullMembers, false);
        // sort elements if enabled and not using chooser dialog
        if (sortElements != 0) {
            elements.sort(new ElementComparator(sortElements));
        }
        context.put("members" + postfix, elements);
        context.put("members", elements);
    }

    public static String velocityEvaluate(
        @NotNull Project project,
        @NotNull Map<String, Object> contextMap,
        Map<String, Object> outputContext,
        String templateMacro) throws GenerateCodeException {
        if (templateMacro == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        try {
            VelocityContext vc = new VelocityContext();

            vc.put("settings", CodeStyleSettingsManager.getSettings(project));
            vc.put("project", project);
            vc.put("helper", GenerationHelper.class);
            vc.put("StringUtil", StringUtil.class);
            vc.put("NameUtil", NameUtil.class);
            vc.put("PsiShortNamesCache", PsiShortNamesCache.class);
            vc.put("JavaPsiFacade", JavaPsiFacade.class);
            vc.put("GlobalSearchScope", GlobalSearchScope.class);
            vc.put("EntryFactory", EntryFactory.class);

            for (String paramName : contextMap.keySet()) {
                vc.put(paramName, contextMap.get(paramName));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Velocity Macro:\n" + templateMacro);
                logger.debug("Executing velocity +++ START +++");
            }
            VelocityEngine velocity = VelocityFactory.getVelocityEngine();
            velocity.evaluate(vc, sw, GenerationUtil.class.getName(), templateMacro);
            if (logger.isDebugEnabled()) {
                logger.debug("Executing velocity +++ END +++");
            }
            if (outputContext != null) {
                for (Object key : vc.getKeys()) {
                    if (key instanceof String) {
                        outputContext.put((String) key, vc.get((String) key));
                    }
                }
            }
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerateCodeException("Error in Velocity code generator", e);
        }

        return StringUtil.convertLineSeparators(sw.getBuffer().toString());
    }

    /**
     * Handles any exception during the executing on this plugin.
     *
     * @param project PSI project
     * @param e the caused exception.
     *
     * @throws RuntimeException is thrown for severe exceptions
     */
    public static void handleException(Project project, Exception e) throws RuntimeException {
        logger.info(e);

        if (e instanceof GenerateCodeException) {
            // code generation error - display velocity error in error dialog so user can identify problem quicker
            Messages.showMessageDialog(project,
                "Velocity error generating code - see IDEA log for more details (stacktrace should be in idea.log):\n" +
                    e.getMessage(), "Warning", Messages.getWarningIcon());
        } else if (e instanceof PluginException) {
            // plugin related error - could be recoverable.
            Messages.showMessageDialog(project, "A PluginException was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(), "Warning",
                Messages.getWarningIcon());
        } else if (e instanceof RuntimeException) {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project, "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(),
                "Error", Messages.getErrorIcon());
            throw (RuntimeException) e; // throw to make IDEA alert user
        } else {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project, "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(),
                "Error", Messages.getErrorIcon());
            throw new RuntimeException(e); // rethrow as runtime to make IDEA alert user
        }
    }

    static List<FieldEntry> getFields(PsiClass clazz) {
        return Arrays.stream(clazz.getFields())
                     .map(f -> EntryFactory.of(f, false))
                     .collect(Collectors.toList());
    }

    static List<FieldEntry> getAllFields(PsiClass clazz) {
        return Arrays.stream(clazz.getAllFields())
                     .map(f -> EntryFactory.of(f, false))
                     .collect(Collectors.toList());
    }

    static List<MethodEntry> getMethods(PsiClass clazz) {
        return Arrays.stream(clazz.getMethods())
                     .map(EntryFactory::of)
                     .collect(Collectors.toList());
    }

    static List<MethodEntry> getAllMethods(PsiClass clazz) {
        return Arrays.stream(clazz.getAllMethods())
                     .map(EntryFactory::of)
                     .collect(Collectors.toList());
    }

    static List<String> getImportList(PsiJavaFile javaFile) {
        PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(importList.getImportStatements())
                     .map(PsiImportStatement::getQualifiedName)
                     .collect(Collectors.toList());
    }

    static List<String> getClassTypeParameters(PsiClass psiClass) {
        return Arrays.stream(psiClass.getTypeParameters()).map(PsiNamedElement::getName).collect(Collectors.toList());
    }

    // ------------------- Experimental -------------------

    public static String parseCodeTemplate(@NotNull CodeTemplate codeTemplate, @NotNull Map<String, Object> context) {
        context = rebuildContext(context);
        try {
            InMemoryJavaCompiler jc = InMemoryJavaCompiler.newInstance();
            jc.useParentClassLoader(classLoader);
            String classPath = parseDependenceClassPath(codeTemplate.template, context);
            jc.useOptions("-classpath", classPath);

            Class<?> clazz = jc.compile(ClassTemplate.CLASS_NAME, codeTemplate.template);
            Object obj = clazz.newInstance();
            Method method = clazz.getDeclaredMethod("build", Map.class);
            return ((String) method.invoke(obj, context));
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    private static Map<String, Object> rebuildContext(Map<String, Object> context){

        Map<String, Object> newContext = Maps.newHashMap();
        for (Entry<String, Object> entry : context.entrySet()) {
            newContext.put(entry.getKey(), GET_VALUE_FUNC.apply(entry));
        }
        return newContext;
    }

    private static final Function<Entry<String, Object>, Object> GET_VALUE_FUNC = (entry) -> {
        Object obj = entry.getValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof ClassEntry) {
            return StaticJavaParser.parse(((ClassEntry) obj).getRaw().getText());
        } else if (obj instanceof MethodEntry) {
            return StaticJavaParser.parseMethodDeclaration(((MethodEntry) obj).getRaw().getText());
        } else if (obj instanceof FieldEntry) {
            return StaticJavaParser.parseStatement(((FieldEntry) obj).getRaw().getText());
        } else if (obj instanceof Collection<?>) {
            Collection<?> memberList = (Collection<?>) obj;
            return memberList.stream().map(member -> {
                if (member instanceof MethodEntry) {
                    return StaticJavaParser.parseMethodDeclaration(((MethodEntry) member).getRaw().getText());
                } else if (member instanceof FieldEntry) {
                    return StaticJavaParser.parseStatement(((FieldEntry) member).getRaw().getText());
                } else {
                    return member;
                }
            }).collect(Collectors.toList());
        }
        return obj;
    };

    private static String parseDependenceClassPath(String sourceCode, Map<String, Object> context) {
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        Set<String> jarPaths = cu.getImports()
          .parallelStream()
          .map(ImportDeclaration::getName)
          .map(Name::asString)
          .filter(fullClassName->!fullClassName.startsWith("java"))
          .distinct()
          .map(fullClassName -> {
              try {
                  return Class.forName(fullClassName);
              } catch (ClassNotFoundException e) {
                  Class<?> clazz = tryInnerClass(fullClassName);
                  if (clazz == null) {
                      logger.error("cannot find class " + fullClassName);
                  }
                  return clazz;
              }
          })
          .filter(Objects::nonNull)
          .map(PathUtil::getJarPathForClass)
          .collect(Collectors.toSet());

        context.values()
               .stream()
               .filter(Objects::nonNull)
               .forEach(obj -> jarPaths.add(obj.getClass().getName()));
        jarPaths.add(System.getProperty("java.class.path"));
        parseThirdPartLib(sourceCode.split(System.lineSeparator())[0], jarPaths);

        return String.join(":", jarPaths);
    }

    private static Class<?> tryInnerClass(String fullClassName) {
        int idx = fullClassName.lastIndexOf(".");
        if (idx > 0) {
            char[] chars = fullClassName.toCharArray();
            chars[idx] = '$';
            String innerClassName = new String(chars);
            try {
                return Class.forName(innerClassName);
            } catch (ClassNotFoundException e) {
                return tryInnerClass(innerClassName);
            }
        }
        return null;
    }

    private static void parseThirdPartLib(String firstLine, Set<String> jarPaths) {
        if (firstLine.contains("libPath=")
            && firstLine.split("=").length == 2) {
            String libPaths = firstLine.split("=")[1];
            for (String libPath : libPaths.split(":")) {
                try {
                    List<String> lib = Files.list(Paths.get(libPath))
                                            .map(Path::getFileName)
                                            .map(Path::toString)
                                            .filter(fileName->fileName.endsWith(".jar"))
                                            .map(fileName -> libPath + fileName)
                                            .collect(Collectors.toList());
                    jarPaths.addAll(lib);
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }
}
