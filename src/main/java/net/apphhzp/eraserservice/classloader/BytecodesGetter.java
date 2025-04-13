package net.apphhzp.eraserservice.classloader;

import apphhzp.lib.ClassHelper;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.module.Configuration;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static apphhzp.lib.ClassHelper.lookup;

public final class BytecodesGetter{
    private BytecodesGetter(){}
    private static final MethodHandle bytecodeGetter;
    private static final MethodHandle getClassLoadingLock;
    private static final VarHandle packageLookupVar;
    private static final VarHandle parentLoadersVar;
    private static final VarHandle configurationVar;
    private static final VarHandle fallbackClassLoaderVar;
    public static boolean useOriginalBytecodes=false;
    static {
        try {
            bytecodeGetter = lookup.findVirtual(ModuleClassLoader.class,"getMaybeTransformedClassBytes", MethodType.methodType(byte[].class,String.class,String.class));
            getClassLoadingLock=lookup.findVirtual(ClassLoader.class,"getClassLoadingLock",MethodType.methodType(Object.class,String.class));
            packageLookupVar=lookup.findVarHandle(ModuleClassLoader.class,"packageLookup", Map.class);
            parentLoadersVar=lookup.findVarHandle(ModuleClassLoader.class,"parentLoaders", Map.class);
            configurationVar=lookup.findVarHandle(ModuleClassLoader.class,"configuration", Configuration.class);
            fallbackClassLoaderVar=lookup.findVarHandle(ModuleClassLoader.class,"fallbackClassLoader", ClassLoader.class);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public static ILaunchPluginService.ITransformerLoader byteCodeLoader;
    @SuppressWarnings("unchecked")
    private static byte[] getMaybeTransformedClassBytes(Object moduleClassLoader, final String name)throws ClassNotFoundException {
        byte[] bytes = new byte[0];
        Throwable suppressed = null;
        Map<String, ResolvedModule> map= (Map<String, ResolvedModule>) packageLookupVar.get(moduleClassLoader);
        Map<String, ClassLoader> parentLoaders= (Map<String, ClassLoader>) parentLoadersVar.get(moduleClassLoader);
        try {
            final String pname = name.substring(0, name.lastIndexOf('.'));
            if (map.containsKey(pname)) {
                bytes = loadFromModule(moduleClassLoader,classNameToModuleName(moduleClassLoader,name), (reader,ref)-> getClassBytes(reader, ref, name));
            } else if (parentLoaders.containsKey(pname)) {
                String cname = name.replace('.','/')+".class";
                try (InputStream is = parentLoaders.get(pname).getResourceAsStream(cname)) {
                    if (is != null)
                        bytes = is.readAllBytes();
                }
            }
        } catch (IOException e) {
            suppressed = e;
        }
        byte[] maybeTransformedBytes = bytes;
        if (maybeTransformedBytes.length == 0) {
            ClassNotFoundException e = new ClassNotFoundException(name);
            if (suppressed != null){
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        return maybeTransformedBytes;
    }

    @SuppressWarnings("unchecked")
    private static String classNameToModuleName(Object moduleClassLoader, final String name) {
        final String pname = name.substring(0, name.lastIndexOf('.'));
        Map<String, ResolvedModule> map= (Map<String, ResolvedModule>) packageLookupVar.get(moduleClassLoader);
        return Optional.ofNullable(map.get(pname)).map(ResolvedModule::name).orElse(null);
    }

    private static <T> T loadFromModule(Object moduleClassLoader,final String moduleName, BiFunction<ModuleReader, ModuleReference, T> lookup) throws IOException {
        Configuration configuration= (Configuration) configurationVar.get(moduleClassLoader);
        ResolvedModule module = configuration.findModule(moduleName).orElseThrow(FileNotFoundException::new);
        ModuleReference ref = module.reference();
        try (ModuleReader reader = ref.open()) {
            return lookup.apply(reader, ref);
        }
    }

    public static <T, E extends Exception> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }
    public static <T, R, E extends Exception> Function<T, R> rethrowFunction(Function_WithExceptions<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    @FunctionalInterface
    public interface Consumer_WithExceptions<T, E extends Exception> {
        void accept(T t) throws E;
    }

    @FunctionalInterface
    public interface Function_WithExceptions<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Stream<InputStream> closeHandler(Optional<InputStream> supplier) {
        final InputStream is = supplier.orElse(null);
        return Optional.ofNullable(is).stream().onClose(() -> Optional.ofNullable(is).ifPresent(rethrowConsumer(InputStream::close)));
    }
    private static byte[] getClassBytes(ModuleReader reader,ModuleReference ref, final String name) {
        String cname = name.replace('.','/')+".class";
        try (Stream<InputStream> istream = closeHandler(Optional.of(reader).flatMap(rethrowFunction(r->r.open(cname))))) {
            return istream.map(rethrowFunction(InputStream::readAllBytes))
                    .findFirst()
                    .orElseGet(()->new byte[0]);
        }
    }

//    private static Class<?> readerToClass(ModuleClassLoader loader,final ModuleReader reader, final ModuleReference ref, final String name) {
//        var bytes = getClassBytes(reader, ref, name);
//        if (bytes.length == 0) return null;
//        var cname = name.replace('.','/')+".class";
//        var modroot = loader.resolvedRoots.get(ref.descriptor().name());
//        ProtectionDomainHelper.tryDefinePackage(loader, name, modroot.jar().getManifest(), t->modroot.jar().getManifest().getAttributes(t), loader::definePackage); // Packages are dirctories, and can't be signed, so use raw attributes instead of signed.
//        var cs = ProtectionDomainHelper.createCodeSource(toURL(ref.location()), modroot.jar().verifyAndGetSigners(cname, bytes));
//        return defineClass(name, bytes, 0, bytes.length, ProtectionDomainHelper.createProtectionDomain(cs, loader));
//    }
//
//    private static Class<?> findClass(ModuleClassLoader loader,final String moduleName, final String name) {
//        try {
//            return loadFromModule(moduleName, (reader, ref) -> loader.readerToClass(reader, ref, name));
//        } catch (IOException e) {
//            return null;
//        }
//    }

    private static Class<?> loadClass(ModuleClassLoader loader,final String name) throws ClassNotFoundException {
        try {
            synchronized (getClassLoadingLock.invoke(loader,name)) {
                Class<?> c = ClassHelper.findLoadedClass(loader,name);
                if (c == null) {
                    int index = name.lastIndexOf('.');
                    Map<String, ResolvedModule> packageLookup= (Map<String, ResolvedModule>) packageLookupVar.get(loader);
                    Map<String, ClassLoader> parentLoaders= (Map<String, ClassLoader>) parentLoadersVar.get(loader);
                    if (index >= 0) {
                        final String pname = name.substring(0, index);
                        if (!packageLookup.containsKey(pname)) {
                            //Fixed
                            c = ClassHelper.findLoadedClass(parentLoaders.getOrDefault(pname, (ClassLoader) fallbackClassLoaderVar.get(loader)),name);
                        }
                    }
                }
                if (c == null) throw new ClassNotFoundException(name);
                return c;
            }
        }catch (ClassNotFoundException t){
            throw t;
        }catch (Throwable t){
            throw new RuntimeException(t);
        }

    }

    public static byte[] getBytecodes(String className) throws ClassNotFoundException {
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        if (!(loader instanceof ModuleClassLoader)){
            throw new RuntimeException("Unexpected ClassLoader:"+loader.getName());
        }
        try {
            if (useOriginalBytecodes){
                return getMaybeTransformedClassBytes(loader,className.replace('/','.'));
            }
            if (byteCodeLoader != null) {
                return byteCodeLoader.buildTransformedClassNodeFor(className.replace('/', '.'));
            }
            return (byte[]) bytecodeGetter.invoke(loader, className.replace('/', '.'), "");
        }catch (ClassNotFoundException e){
            try {
                Class<?> klass=loadClass((ModuleClassLoader) loader,className.replace('/','.'));
                InputStream is=klass.getResourceAsStream("/"+className+".class");
                byte[] dat=new byte[is.available()];
                is.read(dat);
                is.close();
                return dat;
            }catch (ClassNotFoundException e1){
                throw e1;
            }catch (IOException e1){
                throw new ClassNotFoundException("Class not found:"+className+".class",e1);
            }
        }catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
