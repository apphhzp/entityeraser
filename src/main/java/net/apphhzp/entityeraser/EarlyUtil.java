package net.apphhzp.entityeraser;

import apphhzp.lib.ClassHelperSpecial;
import apphhzp.lib.CoremodHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.apphhzp.entityeraser.util.EntityEraserBufferBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static apphhzp.lib.ClassHelperSpecial.lookup;

@SuppressWarnings("unused")
public final class EarlyUtil {
    @OnlyIn(Dist.CLIENT)
    public static void glfwSwapBuffers(long window){
        if (ClassHelperSpecial.findLoadedClass(Thread.currentThread().getContextClassLoader(),"net.minecraft.client.Minecraft")!=null){
            if (Thread.currentThread()== RenderSystem.renderThread){
                EntityEraserBufferBuilder.render();
            }
            GLFW.glfwSwapBuffers(window);
            if (Thread.currentThread()== RenderSystem.renderThread){
                EntityEraserBufferBuilder.render();
            }
        }else {
            GLFW.glfwSwapBuffers(window);
        }
    }

    private static void defineClass(String name){
        try {
            if (ClassHelperSpecial.findLoadedClass(EarlyUtil.class.getClassLoader(),name)==null) {
                ClassHelperSpecial.defineClass(name, CoremodHelper.getBytecodesFromFile(name, EarlyUtil.class), EarlyUtil.class.getClassLoader());
            }
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    private static volatile boolean methodUtilDefined=false;
    private static void defineMethodUtil(){
        if (!methodUtilDefined) {
            methodUtilDefined=true;
            defineClass("net.apphhzp.entityeraser.MethodUtil");
            defineClass("net.apphhzp.entityeraser.MethodUtil$2");
            defineClass("net.apphhzp.entityeraser.MethodUtil$1");
        }
    }

    private static final Class<?> methodAccessorImplKlass;
    private static final Class<?> nativeMethodAccessorImplKlass;
    private static final MethodHandle nmAccessorImplInvoke0;
    private static final MethodHandle mAccessorImplInvoke;
    private static final VarHandle nmAccessorImplMethodVar;
    private static final MethodHandle fillInStackTraceMethod;
    private static final VarHandle stackTraceVar;
    static {
        try {
            methodAccessorImplKlass=Class.forName("jdk.internal.reflect.MethodAccessorImpl");
            nativeMethodAccessorImplKlass=Class.forName("jdk.internal.reflect.NativeMethodAccessorImpl");
            nmAccessorImplInvoke0= lookup.findStatic(nativeMethodAccessorImplKlass,"invoke0", MethodType.methodType(Object.class, Method.class, Object.class, Object[].class));
            mAccessorImplInvoke=lookup.findVirtual(methodAccessorImplKlass,"invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            nmAccessorImplMethodVar=lookup.findVarHandle(nativeMethodAccessorImplKlass,"method", Method.class);
            fillInStackTraceMethod=lookup.findVirtual(Throwable.class,"fillInStackTrace",MethodType.methodType(Throwable.class,int.class));
            stackTraceVar=lookup.findVarHandle(Throwable.class,"stackTrace", StackTraceElement[].class);
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }
//
//    public static Object invoke(Method method, Object obj, Object... args) throws Throwable {
//        boolean[] flg={false};
//        Object re=checkInvoke(method,obj,flg,args);
//        if (flg[0]){
//            return re;
//        }
//        return method.invoke(obj, args);
//    }
//
//    private static Object checkInvoke(Class<?> declaringClass,String name,Class<?> reClass,Class<?>[] argClasses,Object obj,boolean[] changed,Object... args) throws Throwable {
//        if (declaringClass==nativeMethodAccessorImplKlass
//                &&match(name,reClass,argClasses,"invoke0", Object.class,Method.class, Object.class,Object[].class)){
//            changed[0]=true;
//            return invoke0Native((Method) args[0],args[1],(Object[]) args[2]);
//        }else if (declaringClass.isAssignableFrom(methodAccessorImplKlass)
//                &&match(name,reClass,argClasses,"invoke", Object.class,Object.class,Object[].class)){
//            changed[0]=true;
//            return invokeMethodAccessorImpl(obj,args[0],(Object[]) args[1]);
//        }else if (declaringClass.isAssignableFrom(Method.class)
//                &&match(name,reClass,argClasses,"invoke", Object.class,Object.class,Object[].class)){
//            changed[0]=true;
//            return invoke((Method) obj,args[0],(Object[]) args[1]);
//        }else if (EntityEraserTransformerSpecial.hideFromStackTrace&&declaringClass==Throwable.class
//                &&match(name,reClass,argClasses,"fillInStackTrace", Throwable.class,int.class)){
//            changed[0]=true;
//            return fillInStackTrace((Throwable) obj, (Integer) args[0]);
//        }else if (EntityEraserTransformerSpecial.hideFromStackTrace&&declaringClass==Throwable.class
//                &&match(name,reClass,argClasses,"fillInStackTrace", Throwable.class)){
//            changed[0]=true;
//            return fillInStackTrace((Throwable) obj);
//        }else if (declaringClass== GLFW.class
//                &&match(name,reClass,argClasses,"glfwSwapBuffers",void.class,long.class)){
//            changed[0]=true;
//            glfwSwapBuffers((Long) args[0]);
//            return null;
//        }else if (EntityEraserTransformerSpecial.hideFromStackTrace&&declaringClass.isAssignableFrom(Throwable.class)
//                &&match(name,reClass,argClasses,"getStackTrace", StackTraceElement[].class)){
//            changed[0]=true;
//            defineMethodUtil();
//            return MethodUtil.getStackTrace((Throwable) obj);
//        }
//        return null;
//    }
//
//    private static Object checkInvoke(Method method,Object obj,boolean[] changed,Object... args) throws Throwable {
//        return checkInvoke(method.getDeclaringClass(),method.getName(),method.getReturnType(),method.getParameterTypes(),obj,changed,args);
//    }
//
//    private static boolean match(String name,Class<?> reClass,Class<?>[] argClasses,String targetName,Class<?> targetReType,Class<?>... targetArgTypes){
//        if (reClass!=targetReType||!name.equals(targetName)||argClasses.length!=targetArgTypes.length){
//            return false;
//        }
//        for (int i=0;i<argClasses.length;i++){
//            if (argClasses[i]!=targetArgTypes[i]){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static Object invoke0Native(Method method, Object obj, Object[] args)throws Throwable{
//        boolean[] flg={false};
//        Object re=checkInvoke(method,obj,flg,args);
//        if (flg[0]){
//            return re;
//        }
//        return nmAccessorImplInvoke0.invoke(method,obj,args);
//    }
//
//    private static Object invokeMethodAccessorImpl(Object accessorImpl, Object obj, Object[] args)throws Throwable{
//        if (accessorImpl.getClass()==nativeMethodAccessorImplKlass){
//            boolean[] flg={false};
//            Object re=checkInvoke((Method) nmAccessorImplMethodVar.get(accessorImpl),obj,flg,args);
//            if (flg[0]){
//                return re;
//            }
//        }
//        return mAccessorImplInvoke.invoke(accessorImpl,obj,args);
//    }

    public static Throwable fillInStackTrace(Throwable throwable){
        Throwable re= throwable.fillInStackTrace();
        stackTraceVar.set(re, Stream.of((StackTraceElement[])stackTraceVar.get(re))
                .filter((element -> !element.getClassName().startsWith("net.apphhzp.entityeraser")
                        &&!element.getClassName().startsWith("net.apphhzp.eraserservice")))
                .toArray(StackTraceElement[]::new));
        return re;
    }

    private static Throwable fillInStackTrace(Throwable throwable,int dummy) throws Throwable{
        Throwable re= (Throwable) fillInStackTraceMethod.invoke(throwable,dummy);
        stackTraceVar.set(re,Stream.of((StackTraceElement[])stackTraceVar.get(re))
                .filter((element -> !element.getClassName().startsWith("net.apphhzp.entityeraser")
                        &&!element.getClassName().startsWith("net.apphhzp.eraserservice")))
                .toArray(StackTraceElement[]::new));
        return re;
    }

    public static StackTraceElement[] getStackTrace(Throwable throwable){
        stackTraceVar.set(throwable,Stream.of((StackTraceElement[])stackTraceVar.get(throwable))
                .filter((element -> !element.getClassName().startsWith("net.apphhzp.entityeraser")
                        &&!element.getClassName().startsWith("net.apphhzp.eraserservice")))
                .toArray(StackTraceElement[]::new));
        return (StackTraceElement[])stackTraceVar.get(throwable);
    }
//
//    public static Object invoke(MethodHandle methodHandle,Object... args){
//        try {
//            return methodHandle.invoke(args);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//    }
}
