package me.blindedbythesun.nya.modules.efly.impl;

import me.blindedbythesun.nya.modules.efly.ElytraFlyPlusMode;
import me.blindedbythesun.nya.modules.efly.ElytraFlyPlusModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class ElytraFlyPlusSimple extends ElytraFlyPlusMode {

    public ElytraFlyPlusSimple() {
        super(ElytraFlyPlusModes.Simple);
    }

    boolean shouldFreeze() {
        return !(mc.options.forwardKey.isPressed() || mc.options.sneakKey.isPressed() || mc.options.jumpKey.isPressed());
    }

    double frozenMotionX, frozenMotionY, frozenMotionZ;
    public float cameraPitch = 0;
    boolean isFrozen = false;
    boolean wasFlying = false;

    @Override
    public void onActivate() {
        cameraPitch = mc.player.getPitch();
    }

    @Override
    public void onDeactivate() {
        if(mc.player.isFallFlying()) {
            mc.player.setPitch(cameraPitch);
        }
    }

    public void onPreTick(TickEvent.Pre event) {
        if(!wasFlying && mc.player.isFallFlying()) {
            cameraPitch = mc.player.getPitch();
        }else if(wasFlying && !mc.player.isFallFlying()) {
            mc.player.setPitch(cameraPitch);
        }
        wasFlying = mc.player.isFallFlying();

        if(mc.player.isFallFlying()) {
            if(!shouldFreeze()) {
                if(isFrozen) {
                    isFrozen = false;
                    //mc.player.setVelocity(frozenMotionX,frozenMotionY,frozenMotionY);
                }

                double yaw = Math.toRadians(mc.player.getYaw());;

                float movementSpeed = (float) Math.sqrt(Math.pow(mc.player.getVelocity().x, 2) + Math.pow(mc.player.getVelocity().z, 2));
                if(movementSpeed < settings.maxSpeedSimple.get()/20d) {
                    // If you aim up you cant accelerate as fast without flagging. (Constantiam)
                    double speed = mc.options.jumpKey.isPressed() ? settings.verticalSpeed.get() : settings.horizontalAcceleration.get();
                    speed *= 1+Math.min(0.2,movementSpeed/7);

                    double addX = -Math.sin(yaw) * speed;
                    double addZ = Math.cos(yaw) * speed;

                    if(mc.options.forwardKey.isPressed() || mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x + addX, mc.player.getVelocity().y, mc.player.getVelocity().z + addZ);
                    }
                }

                if(movementSpeed > 0.6) {
                    float pitch = (float) (mc.options.jumpKey.isPressed() ? -settings.verticalAngle.get() : (mc.options.sneakKey.isPressed() ? settings.verticalAngle.get() : -8.175d / movementSpeed));
                    mc.player.setPitch(pitch);
                }else mc.player.setPitch(0f);
            }else {
                if(!isFrozen) {
                    isFrozen = true;
                    frozenMotionX = mc.player.getVelocity().x;
                    frozenMotionY = mc.player.getVelocity().y;
                    frozenMotionZ = mc.player.getVelocity().z;
                }
                mc.player.setVelocity(0,0,0);
            }
        }else if(isFrozen) isFrozen = false;
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if(isFrozen && event.packet instanceof PlayerMoveC2SPacket) {
            //event.cancel();
        }
    }
}
