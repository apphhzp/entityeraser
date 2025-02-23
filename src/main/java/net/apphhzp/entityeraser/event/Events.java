package net.apphhzp.entityeraser.event;

import net.apphhzp.entityeraser.item.TestItem;
import net.apphhzp.entityeraser.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

import static net.apphhzp.entityeraser.init.EntityeraserModItems.ENTITY_ERASER;
import static net.apphhzp.entityeraser.init.EntityeraserModItems.ENTITY_PROTECTOR;

@Mod.EventBusSubscriber
public class Events {
	@SubscribeEvent
	public static void  onRender(TickEvent.RenderTickEvent event) {
		Minecraft mc= Minecraft.getInstance();

//		if (event.phase==TickEvent.Phase.END) {
		if (TestItem.fboId!=0) {
//			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, TestItem.fboId);
//			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,0);
//			GL11.glDrawBuffer(GL11.GL_BACK);
//			GL30.glBlitFramebuffer(0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
//			GL11.glDrawBuffer(GL11.GL_LEFT);
//			GL30.glBlitFramebuffer(0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
//			GL11.glDrawBuffer(GL11.GL_BACK);
//			GL30.glBlitFramebuffer(0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
//			GL11.glDrawBuffer(GL11.GL_FRONT);
//			GL30.glBlitFramebuffer(0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					0, 0, mc.window.getWidth(), mc.window.getHeight(),
//					GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
//			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,0);
//			GL11.glDrawBuffer(GL11.GL_BACK);
//			GL11.glFinish();
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Player player=event.player;
			if (EntityUtil.shouldDie(player)){
				EntityUtil.killEntity(player);
			}else if (EntityUtil.shouldProtect(player)){
				EntityUtil.defense(player);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderTooltipColor(RenderTooltipEvent.Color event){
		if (event.getItemStack().getItem()==ENTITY_ERASER.get()||event.getItemStack().getItem()==ENTITY_PROTECTOR.get()){
			event.setBackgroundStart(EntityUtil.getSmoothColor(192,0,1000D));
			event.setBackgroundEnd(EntityUtil.getSmoothColor(128,Math.PI/3D*2D,1000D));
			event.setBorderStart(EntityUtil.getSmoothColor(128,Math.PI/2D,1000D));
			event.setBorderEnd(EntityUtil.getSmoothColor(128,Math.PI/2D*3D,1000D));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST,receiveCanceled = true)
	public static void afterLevelRender(TickEvent.RenderTickEvent event){
		if (event.phase==TickEvent.Phase.END){
			if (EntityUtil.shouldDie(Minecraft.getInstance().player)){
				EntityUtil.setAndRenderDeath();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST,receiveCanceled = true)
	public static void afterGuiRender(RenderGuiEvent.Post event){
		Minecraft mc=Minecraft.getInstance();
		if (EntityUtil.shouldDie(mc.player)){
			EntityUtil.setAndRenderDeath();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST,receiveCanceled = true)
	public static void beforeGuiRender(RenderGuiEvent.Pre event){
		Minecraft mc=Minecraft.getInstance();
		if (EntityUtil.shouldProtect(mc.player)){
			if (EntityUtil.isDeathScreen(mc.screen)){
				EntityUtil.clearScreen(mc);
				event.setCanceled(true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST,receiveCanceled = true)
	public static void beforeScreenOpen(ScreenEvent.Opening event){
		Minecraft mc=Minecraft.getInstance();
		if (EntityUtil.shouldProtect(mc.player)){
			if (EntityUtil.isDeathScreen(event.getCurrentScreen())||EntityUtil.isDeathScreen(event.getNewScreen())){
				EntityUtil.clearScreen(mc);
				//noinspection DataFlowIssue
				event.setNewScreen(null);
				event.setCanceled(true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void beforeScreenInit(ScreenEvent.Init.Pre event){
		Minecraft mc=Minecraft.getInstance();
		if (EntityUtil.shouldProtect(mc.player)){
			if (EntityUtil.isDeathScreen(event.getScreen())){
				EntityUtil.clearScreen(mc);
				event.setCanceled(true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void beforeScreenRender(ScreenEvent.Render.Pre event){
		Minecraft mc=Minecraft.getInstance();
		if (EntityUtil.shouldProtect(mc.player)){
			if (EntityUtil.isDeathScreen(event.getScreen())){
				EntityUtil.clearScreen(mc);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityJoinLevel(EntityJoinLevelEvent event){
		if (EntityUtil.disableSpawn&&!(event.getEntity() instanceof Player)){
			event.setCanceled(true);
		}
		if (EntityUtil.isDeadEntity(event.getEntity())){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingEntityTick(LivingEvent.LivingTickEvent event){
		if ((EntityUtil.disableSpawn&&!(event.getEntity() instanceof Player))||EntityUtil.isDeadEntity(event.getEntity())){
			EntityUtil.killEntity(event.getEntity());
			event.setCanceled(true);
		}
		if (EntityUtil.shouldProtect(event.getEntity())){
			EntityUtil.defense((Player) event.getEntity());
		}
	}

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event){
		if (event.phase== TickEvent.Phase.END){
			if (EntityUtil.disableSpawn){
				Level world= event.level;
				Set<Entity> set=new HashSet<>();
				if (world.isClientSide){
					if (world instanceof ClientLevel clientLevel){
						set.addAll(clientLevel.players);
						set.addAll(clientLevel.entityStorage.entityStorage.byId.values());
						set.addAll(clientLevel.entityStorage.entityStorage.byUuid.values());
					}
				}else {
					if (world instanceof ServerLevel serverLevel){
						set.addAll(serverLevel.players);
						set.addAll(serverLevel.entityManager.visibleEntityStorage.byId.values());
						set.addAll(serverLevel.entityManager.visibleEntityStorage.byUuid.values());
					}
				}
				for (Entity target:set){
					if (!(target instanceof Player)){
						EntityUtil.killEntity(target);
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityAttack(LivingAttackEvent event){
		if (EntityUtil.shouldProtect(event.getEntity())){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityDie(LivingDeathEvent event){
		if (EntityUtil.shouldProtect(event.getEntity())){
			EntityUtil.defense((Player) event.getEntity());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityHurt(LivingHurtEvent event){
		if (EntityUtil.shouldProtect(event.getEntity())){
			EntityUtil.defense((Player) event.getEntity());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityDamage(LivingDamageEvent event){
		if (EntityUtil.shouldProtect(event.getEntity())){
			EntityUtil.defense((Player) event.getEntity());
			event.setCanceled(true);
		}
	}
}
