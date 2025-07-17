package net.apphhzp.entityeraser.util;

public class Test {
    public static void main(String[] args) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException {
//        try {
//            //instImpl.addTransformer(new EntityEraserClassFileTransformerSpecial(), true);
//            Class<?> implClass=Class.forName("sun.instrument.InstrumentationImpl");
//            InputStream is=implClass.getResourceAsStream("/sun/instrument/InstrumentationImpl.class");
//            byte[] dat=new byte[is.available()];
//            is.read(dat);
//            is.close();
//            ClassNode classNode= CoremodHelper.bytes2ClassNote(dat,"sun.instrument.InstrumentationImpl");
//            for (MethodNode method : classNode.methods) {
//                if ("transform".equals(method.name) && "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B".equals(method.desc)) {
//                    method.instructions.clear();
//                    method.localVariables.clear();
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,4));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,5));
//                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD,6));
//                    method.instructions.add(new VarInsnNode(Opcodes.ILOAD,7));
//                    method.visitMethodInsn(Opcodes.INVOKESTATIC, "net/apphhzp/eraserservice/agent/EntityEraserClassFileTransformerSpecial", "transform", "(Lsun/instrument/InstrumentationImpl;Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B", false);
//                    method.instructions.add(new InsnNode(Opcodes.ARETURN));
//                    method.visitMaxs(8,8);
//                }
//            }
//            dat=CoremodHelper.classNote2bytes(classNode,true);
//            instImpl.redefineClasses(new ClassDefinition(implClass,dat));
//        }catch (Throwable t){
//            throw new RuntimeException(t);
//        }

        //SharedLibrary lib = APIUtil.apiCreateLibrary("C:\\Program Files\\Java\\jdk-17\\bin\\server\\jvm.dll");
//        System.err.println(lib.getFunctionAddress(""));
    }
}
