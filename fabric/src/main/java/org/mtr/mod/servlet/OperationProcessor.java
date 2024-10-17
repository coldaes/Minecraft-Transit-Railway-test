package org.mtr.mod.servlet;

import org.mtr.core.integration.Response;
import org.mtr.core.operation.PlayerPresentResponse;
import org.mtr.core.operation.UpdateDataResponse;
import org.mtr.core.operation.VehicleLiftResponse;
import org.mtr.core.servlet.QueueObject;
import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.PlayerEntity;
import org.mtr.mapping.holder.ServerPlayerEntity;
import org.mtr.mapping.holder.ServerWorld;
import org.mtr.mod.Init;
import org.mtr.mod.packet.PacketUpdateData;
import org.mtr.mod.packet.PacketUpdateVehiclesLifts;

import java.util.UUID;

public final class OperationProcessor {

	public static void process(QueueObject queueObject, ServerWorld serverWorld, String dimension) {
		switch (queueObject.key) {
			case "vehicles-lifts":
				if (queueObject.data instanceof VehicleLiftResponse) {
					final PlayerEntity playerEntity = serverWorld.getPlayerByUuid(UUID.fromString(((VehicleLiftResponse) queueObject.data).getClientId()));
					if (playerEntity == null) {
						queueObject.runCallback(Utilities.getJsonObjectFromData(new PlayerPresentResponse("")));
					} else {
						Init.REGISTRY.sendPacketToClient(ServerPlayerEntity.cast(playerEntity), new PacketUpdateVehiclesLifts(new Response(200, System.currentTimeMillis(), "", Utilities.getJsonObjectFromData(queueObject.data))));
						queueObject.runCallback(Utilities.getJsonObjectFromData(new PlayerPresentResponse(dimension)));
					}
				}
				break;
			case "refresh-depot":
				if (queueObject.data instanceof UpdateDataResponse) {
					PacketUpdateData.sendDirectlyToClientDepotUpdate(serverWorld, (UpdateDataResponse) queueObject.data);
				}
				break;
		}

	}
}
