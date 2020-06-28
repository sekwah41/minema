package info.ata4.minecraft.minema.shaderHook_coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;
import java.util.ListIterator;

public final class ShaderHookInjector implements IClassTransformer {

	// All obfuscated/deobfuscated mappings can be found in the .gradle
	// directory (usually inside user directory) in
	// .gradle\caches\minecraft\de\oceanlabs\mcp\mcp_snapshot\XXXXXXXX\X.XX\srgs\mcp-notch.srg:
	// MCP Mappings for all classes, methods and fields
	// Do not use methods.csv etc. because those are the Forge mappings (which
	// is only relevant for runtime reflection)

	private static final String entityRenderer = "net.minecraft.client.renderer.EntityRenderer";
	private static final String minecraftServer = "net.minecraft.server.MinecraftServer";
	private static final String screenshotHelper = "net.minecraft.util.ScreenShotHelper";
	private static final String openGLHelper = "net.minecraft.client.renderer.OpenGlHelper";
	private static final String glStateManager = "net.minecraft.client.renderer.GlStateManager";

	@Override
	public byte[] transform(final String obfuscated, final String deobfuscated, final byte[] bytes) {
		// "Deobfuscated" is always passed as a deobfuscated argument, but the
		// "obfuscated" argument may be deobfuscated or obfuscated
		if (entityRenderer.equals(deobfuscated) || minecraftServer.equals(deobfuscated)
				|| screenshotHelper.equals(deobfuscated)
				|| openGLHelper.equals(deobfuscated)
				|| glStateManager.equals(deobfuscated)) {

			final ClassReader classReader = new ClassReader(bytes);
			final ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);

			boolean isInAlreadyDeobfuscatedState = obfuscated.equals(deobfuscated);

			if (entityRenderer.equals(deobfuscated)) {
				this.transformEntityRenderer(classNode, isInAlreadyDeobfuscatedState);
			} else if (minecraftServer.equals(deobfuscated)) {
				this.transformMinecraftServer(classNode, isInAlreadyDeobfuscatedState);
			} else if (screenshotHelper.equals(deobfuscated)) {
				this.transformScreenshotHelper(classNode, isInAlreadyDeobfuscatedState);
			} else if (openGLHelper.equals(deobfuscated)) {
				this.transformOpenGLHelper(classNode, isInAlreadyDeobfuscatedState);
			} else if (glStateManager.equals(deobfuscated)) {
				this.transformGlStateManager(classNode, isInAlreadyDeobfuscatedState);
			}

			final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(classWriter);
			return classWriter.toByteArray();

		}

		return bytes;
	}

	private void transformEntityRenderer(ClassNode classNode, boolean isInAlreadyDeobfuscatedState) {
		final String method = isInAlreadyDeobfuscatedState ? "renderWorld" : "b";

		for (final MethodNode m : classNode.methods) {
			if (method.equals(m.name) && "(FJ)V".equals(m.desc)) {
				// after the GLStateManager.enableDepth call:
				// that is right after Optifine patches the source code to
				// call shadersmod/client/Shaders#beginRender which includes
				// the initialization of frameTimeCounter

				String calledClass = isInAlreadyDeobfuscatedState ? "net/minecraft/client/renderer/GlStateManager" : "bus";
				String calledMethod = isInAlreadyDeobfuscatedState ? "enableDepth" : "k";

				// find it (insert and insertBefore do not work because
				// nodes build the actual recursive data structure and the
				// location has to be an actual member of the data)

				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();

				while (iterator.hasNext()) {
					AbstractInsnNode currentNode = iterator.next();
					if (doesMatchStaticCall(currentNode, calledClass, calledMethod, "()V")) {
						iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
								"info/ata4/minecraft/minema/client/modules/ShaderSync",
								"setFrameTimeCounter", "()V", false));
						break;
					}
				}
			} else if (m.name.equals("a") && m.desc.equals("(IFJ)V")) {
				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();

				while (iterator.hasNext()) {
					AbstractInsnNode currentNode = iterator.next();

					if (currentNode.getOpcode() == Opcodes.LDC) {
						LdcInsnNode ldc = (LdcInsnNode) currentNode;

						if ("hand".equals(ldc.cst)) {
							currentNode = iterator.next();
							iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "info/ata4/minecraft/minema/CaptureSession", "ASMmidRender", "()V", false));

							break;
						}
					}
				}
			}
		}
	}

	private void transformMinecraftServer(ClassNode classNode, boolean isInAlreadyDeobfuscatedState) {
		for (MethodNode method : classNode.methods) {
			if (method.name.equals("run")) {
				int i = 0;
				Iterator<AbstractInsnNode> nodes = method.instructions.iterator();
				AbstractInsnNode target = null;

				while (nodes.hasNext()) {
					AbstractInsnNode node = nodes.next();

					if (node instanceof VarInsnNode) {
						VarInsnNode var = (VarInsnNode) node;

						if (var.getOpcode() == Opcodes.LSTORE && var.var == 1) {
							if (i == 1) {
								target = var;
							}

							i += 1;
						}
					}
				}

				if (target != null) {
					InsnList list = new InsnList();

					list.add(new VarInsnNode(Opcodes.LLOAD, 1));
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "info/ata4/minecraft/minema/client/modules/ShaderSync", "correctServerTick", "(J)J", false));
					list.add(new VarInsnNode(Opcodes.LSTORE, 1));

					method.instructions.insert(target, list);
				}
			}
		}
	}

	private void transformScreenshotHelper(ClassNode classNode, boolean isInAlreadyDeobfuscatedState) {
		for (MethodNode method : classNode.methods) {
			if (!method.desc.contains("BufferedImage")) {
				continue;
			}

			boolean passedBufferImage = false;
			AbstractInsnNode target = null;
			Iterator<AbstractInsnNode> it = method.instructions.iterator();

			while (it.hasNext())
			{
				AbstractInsnNode node = it.next();

				if (passedBufferImage && node.getOpcode() == Opcodes.ICONST_1) {
					target = node;

					break;
				} else if (node.getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.endsWith("BufferedImage"))  {
					passedBufferImage = true;

					continue;
				}
			}

			if (target != null)
			{
				AbstractInsnNode node = new MethodInsnNode(Opcodes.INVOKESTATIC, "info/ata4/minecraft/minema/client/util/ScreenshotHelper", "getType", "()I", false);

				method.instructions.insert(target, node);
				method.instructions.remove(target);
			}
		}
	}

	/**
	 * look at {@link net.minecraftforge.client.GuiIngameForge#renderChat(int, int)}
	 * @param classNode
	 * @param isInAlreadyDeobfuscatedState
	 */
	private void transformOpenGLHelper(ClassNode classNode, boolean isInAlreadyDeobfuscatedState) {

		// test blendFunc tho may need to alter more

		for (MethodNode method : classNode.methods) {
			/**
			 * Look at it though the GLStateManager may be more important
			 * {@link net.minecraft.client.renderer.OpenGlHelper#glBlendFunc(int, int, int, int)}
			 */
			//System.out.println(method);
		}
	}

	private void transformGlStateManager(ClassNode classNode, boolean isInAlreadyDeobfuscatedState) {

		// test blendFunc tho may need to alter more
		for (MethodNode method : classNode.methods) {

			/**
			 * {@link net.minecraft.client.renderer.GlStateManager#blendFunc(int, int)}
			 */
			if(method.name.equals("blendFunc") && method.desc.equals("(II)V")) {

				AbstractInsnNode invokeNode = null;
				Iterator<AbstractInsnNode> it = method.instructions.iterator();

				while (it.hasNext())
				{
					AbstractInsnNode node = it.next();
					if (node.getOpcode() == Opcodes.INVOKESTATIC) {
						invokeNode = node;
					}
				}

				if(invokeNode != null) {
					AbstractInsnNode replacementNode = new MethodInsnNode(Opcodes.INVOKESTATIC,
							"info/ata4/minecraft/minema/client/util/ScreenshotHelper", "replaceBlendFunc",
							"(II)V", false);
					method.instructions.insert(invokeNode, replacementNode);
					method.instructions.remove(invokeNode);
				}
			}

			/**
			 * {@link net.minecraft.client.renderer.GlStateManager#tryBlendFuncSeparate(int, int, int, int)}
			 */
			if(method.name.equals("tryBlendFuncSeparate") && method.desc.equals("(IIII)V")) {

				AbstractInsnNode invokeNode = null;
				Iterator<AbstractInsnNode> it = method.instructions.iterator();

				while (it.hasNext())
				{
					AbstractInsnNode node = it.next();
					if (node.getOpcode() == Opcodes.INVOKESTATIC) {
						invokeNode = node;
					}
				}

				if(invokeNode != null) {
					AbstractInsnNode replacementNode = new MethodInsnNode(Opcodes.INVOKESTATIC,
							"info/ata4/minecraft/minema/client/util/ScreenshotHelper", "tryBlendFuncSeparate",
							"(IIII)V", false);
					method.instructions.insert(invokeNode, replacementNode);
					method.instructions.remove(invokeNode);
				}
			}
		}
	}

	private boolean doesMatchStaticCall(AbstractInsnNode node, String calledClass, String calledMethod, String signature) {
		if (node.getOpcode() == Opcodes.INVOKESTATIC) {
			MethodInsnNode methodCall = (MethodInsnNode) node;
			if (methodCall.owner.equals(calledClass) && methodCall.name.equals(calledMethod)
					&& methodCall.desc.equals(signature)) {
				return true;
			}
		}

		return false;
	}

}
