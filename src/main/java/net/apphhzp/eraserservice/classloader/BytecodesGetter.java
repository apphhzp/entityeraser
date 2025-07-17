package net.apphhzp.eraserservice.classloader;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.ClassOption;
import apphhzp.lib.OnlyInDefineClassHelper;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static apphhzp.lib.ClassHelperSpecial.*;


public final class BytecodesGetter{
    private BytecodesGetter(){}
    private static final MethodHandle bytecodeGetter;
    private static final MethodHandle getClassLoadingLock;
    private static final VarHandle packageLookupVar;
    private static final VarHandle parentLoadersVar;
    private static final VarHandle configurationVar;
    private static final VarHandle fallbackClassLoaderVar;
    public static boolean useOriginalBytecodes=false;
    private static final MethodHandle BiFunction1Constructor;
    private static final MethodHandle Supplier1Constructor;
    private static final MethodHandle Function1Constructor;
    private static final MethodHandle Function2Constructor;
    private static final MethodHandle Runnable1Constructor;
    static {
        try {
            bytecodeGetter = lookup.findVirtual(ModuleClassLoader.class,"getMaybeTransformedClassBytes", MethodType.methodType(byte[].class,String.class,String.class));
            getClassLoadingLock=lookup.findVirtual(ClassLoader.class,"getClassLoadingLock",MethodType.methodType(Object.class,String.class));
            packageLookupVar=lookup.findVarHandle(ModuleClassLoader.class,"packageLookup", Map.class);
            parentLoadersVar=lookup.findVarHandle(ModuleClassLoader.class,"parentLoaders", Map.class);
            configurationVar=lookup.findVarHandle(ModuleClassLoader.class,"configuration", Configuration.class);
            fallbackClassLoaderVar=lookup.findVarHandle(ModuleClassLoader.class,"fallbackClassLoader", ClassLoader.class);
            BiFunction1Constructor=lookup.findConstructor(defineHiddenClassWithClassData("net.apphhzp.eraserservice.classloader.BytecodesGetter$BiFunction1",lookup.findStatic(BytecodesGetter.class,"getClassBytes", MethodType.methodType(byte[].class, ModuleReader.class, ModuleReference.class, String.class)), BytecodesGetter.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class,String.class));
            Supplier1Constructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Supplier1", BytecodesGetter.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class));
            Function1Constructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function1", BytecodesGetter.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class,String.class));
            Function2Constructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Function2", BytecodesGetter.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class));
            Runnable1Constructor=lookup.findConstructor(defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Runnable1", BytecodesGetter.class,true,null, ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class,InputStream.class));
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
                try {
                    bytes = loadFromModule(moduleClassLoader,classNameToModuleName(moduleClassLoader,name), (BiFunction<ModuleReader, ModuleReference, byte[]>) BiFunction1Constructor.invoke(name));
                }catch (Throwable t){
                    throwOriginalException(t);
                    throw new RuntimeException("How did you get here?",t);
                }
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
        try {
            //EntityEraserServiceSpecial.debug();
            ModuleReader reader = ref.open();
            return lookup.apply(reader, ref);
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
        }
    }



    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Stream<InputStream> closeHandler(Optional<InputStream> supplier) {
        final InputStream is = supplier.orElse(null);
        try {
            return Optional.ofNullable(is).stream().onClose((Runnable) Runnable1Constructor.invoke(is));
        } catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException("How did you get here?",e);
        }
    }
    private static byte[] getClassBytes(ModuleReader reader,ModuleReference ref, final String name) {
        String cname = name.replace('.','/')+".class";
        try {
            Stream<InputStream> istream = closeHandler(Optional.of(reader).flatMap((Function<ModuleReader, Optional<? extends InputStream>> )Function1Constructor.invoke(cname)));
            return istream.map((Function<InputStream, byte[]>)(Function2Constructor.invoke()))
                    .findFirst()
                    .orElseGet((Supplier<? extends byte[]>) Supplier1Constructor.invoke());
        }catch (Throwable t){
            throwOriginalException(t);
            throw new RuntimeException("How did you get here?",t);
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
                Class<?> c = ClassHelperSpecial.findLoadedClass(loader,name);
                if (c == null) {
                    int index = name.lastIndexOf('.');
                    Map<String, ResolvedModule> packageLookup= (Map<String, ResolvedModule>) packageLookupVar.get(loader);
                    Map<String, ClassLoader> parentLoaders= (Map<String, ClassLoader>) parentLoadersVar.get(loader);
                    if (index >= 0) {
                        final String pname = name.substring(0, index);
                        if (!packageLookup.containsKey(pname)) {
                            //Fixed
                            c = ClassHelperSpecial.findLoadedClass(parentLoaders.getOrDefault(pname, (ClassLoader) fallbackClassLoaderVar.get(loader)),name);
                        }
                    }
                }
                if (c == null) throw new ClassNotFoundException(name);
                return c;
            }
        }catch (ClassNotFoundException t){
            throw t;
        }catch (Throwable t){
            throwOriginalException(t);
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
                if (klass == null&&!useOriginalBytecodes) {
                    throw e;
                }
                InputStream is=klass!=null?klass.getResourceAsStream("/"+className+".class"):loader.getResourceAsStream("/"+className+".class");
                byte[] dat=new byte[is.available()];
                is.read(dat);
                is.close();
                dat= OnlyInDefineClassHelper.handle(dat,className);
                return dat;
            } catch (IOException e1){
                throw new ClassNotFoundException("Class not found:"+className+".class",e1);
            }
        }catch (Throwable e) {
            throwOriginalException(e);
            throw new RuntimeException(e);
        }
    }

    private static class BiFunction1 implements BiFunction<ModuleReader, ModuleReference, byte[]> {
        private final String name;
        private static final MethodHandle getClassBytesMethod;
        static {
            getClassBytesMethod=ClassHelperSpecial.getClassData(BiFunction1.class);
        }

        public BiFunction1(String name) {
            this.name = name;
        }

        @Override
        public byte[] apply(ModuleReader reader, ModuleReference ref) {
            try {
                return (byte[]) getClassBytesMethod.invoke(reader, ref, name);
            } catch (Throwable e) {
                throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
        }
    }

    private static class Consumer1 implements Consumer<InputStream> {
        @Override
        public void accept(InputStream inputStream) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throwOriginalException(e);
            }
        }
    }

    private static class Function1 implements Function<ModuleReader, Optional<? extends InputStream>> {
        private final String cname;

        public Function1(String cname) {
            this.cname = cname;
        }

        @Override
        public Optional<? extends InputStream> apply(ModuleReader r) {
            try {
                return r.open(cname);
            } catch (IOException e) {
                throwOriginalException(e);
                return Optional.empty();
            }
        }
    }

    private static class Function2 implements Function<InputStream, byte[]> {
        @Override
        public byte[] apply(InputStream inputStream) {
            try {
                return inputStream.readAllBytes();
            } catch (IOException e) {
                throwOriginalException(e);
                return null;
            }
        }
    }

    private static class Supplier1 implements Supplier<byte[]> {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    }

    private static class Runnable1 implements Runnable {
        private final InputStream is;
        private static final MethodHandle Consumer1Constructor;
        static {
            try {
                Consumer1Constructor=lookup.findConstructor(ClassHelperSpecial.defineHiddenClass("net.apphhzp.eraserservice.classloader.BytecodesGetter$Consumer1",Runnable1.class,true,null,ClassOption.STRONG,ClassOption.NESTMATE).lookupClass(),MethodType.methodType(void.class));
            }catch (Throwable t){
                ClassHelperSpecial.throwOriginalException(t);
                throw new RuntimeException("How did you get here?",t);
            }
        }

        public Runnable1(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                Optional.ofNullable(is).ifPresent((Consumer<? super InputStream>) Consumer1Constructor.invoke());
            } catch (Throwable e) {
                ClassHelperSpecial.throwOriginalException(e);
                throw new RuntimeException("How did you get here?",e);
            }
        }
    }
}
