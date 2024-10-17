package org.mtr.mod.packet;

import org.mtr.core.data.Rail;
import org.mtr.core.data.SignalModification;
import org.mtr.core.integration.Response;
import org.mtr.core.operation.UpdateDataRequest;
import org.mtr.core.operation.UpdateDataResponse;
import org.mtr.core.serializer.JsonReader;
import org.mtr.core.serializer.SerializedDataBase;
import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.ServerWorld;
import org.mtr.mapping.mapper.MinecraftServerHelper;
import org.mtr.mapping.tool.PacketBufferReceiver;
import org.mtr.mod.Init;
import org.mtr.mod.client.DynamicTextureCache;
import org.mtr.mod.client.MinecraftClientData;

import javax.annotation.Nonnull;

public final class PacketUpdateData extends PacketRequestResponseBase {

	public PacketUpdateData(PacketBufferReceiver packetBufferReceiver) {
		super(packetBufferReceiver);
	}

	public PacketUpdateData(UpdateDataRequest updateDataRequest) {
		super(Utilities.getJsonObjectFromData(updateDataRequest).toString());
	}

	private PacketUpdateData(String content) {
		super(content);
	}

	@Override
	protected void runClientInbound(Response response) {
		update(response);
	}

	@Override
	protected PacketRequestResponseBase getInstance(String content) {
		return new PacketUpdateData(content);
	}

	@Override
	protected SerializedDataBase getDataInstance(JsonReader jsonReader) {
		return new UpdateDataRequest(jsonReader, new MinecraftClientData());
	}

	@Nonnull
	@Override
	protected String getEndpoint() {
		return "update-data";
	}

	@Override
	protected PacketRequestResponseBase.ResponseType responseType() {
		return PacketRequestResponseBase.ResponseType.ALL;
	}

	public static void sendDirectlyToServerRail(ServerWorld serverWorld, Rail rail) {
		new PacketUpdateData(new UpdateDataRequest(new MinecraftClientData()).addRail(rail)).runServerOutbound(serverWorld, null);
	}

	public static void sendDirectlyToServerSignalModification(ServerWorld serverWorld, SignalModification signalModification) {
		new PacketUpdateData(new UpdateDataRequest(new MinecraftClientData()).addSignalModification(signalModification)).runServerOutbound(serverWorld, null);
	}

	public static void sendDirectlyToClientDepotUpdate(ServerWorld serverWorld, UpdateDataResponse updateDataResponse) {
		MinecraftServerHelper.iteratePlayers(serverWorld, serverPlayerEntityNew -> Init.REGISTRY.sendPacketToClient(serverPlayerEntityNew, new PacketUpdateData(new Response(200, System.currentTimeMillis(), "", Utilities.getJsonObjectFromData(updateDataResponse)).getJson().toString())));
	}

	public static void update(Response response) {
		final MinecraftClientData minecraftClientData = MinecraftClientData.getInstance();
		response.getData(jsonReader -> new UpdateDataResponse(jsonReader, minecraftClientData)).write();
		response.getData(jsonReader -> new UpdateDataResponse(jsonReader, MinecraftClientData.getDashboardInstance())).write();
		minecraftClientData.vehicles.forEach(vehicle -> vehicle.vehicleExtraData.immutablePath.forEach(pathData -> pathData.writePathCache(minecraftClientData, vehicle.getTransportMode())));
		DynamicTextureCache.instance.reload();
	}
}
