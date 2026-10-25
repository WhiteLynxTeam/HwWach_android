package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.remote.api.AssetRolesApi
import ed.maevski.hwwach.data.remote.model.asset.AckRequest
import ed.maevski.hwwach.data.remote.model.asset.AssetDetailDto
import ed.maevski.hwwach.data.remote.model.asset.AssetRoleDto
import ed.maevski.hwwach.data.remote.model.asset.AssignPersonRequest
import ed.maevski.hwwach.data.remote.model.asset.CreateRepairRequestDto
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRolesRepository
import ed.maevski.hwwach.domain.models.AckStatus
import ed.maevski.hwwach.domain.models.AssetRoleInfo
import ed.maevski.hwwach.domain.models.AssetServerInfo
import ed.maevski.hwwach.domain.models.Assignment
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class AssetRolesRepositoryImpl @Inject constructor(
    @Named("api") private val api: AssetRolesApi,
    private val responseErrorMapper: ResponseErrorMapper,
) : IAssetRolesRepository {

    private suspend fun <T> call(block: suspend () -> DomainResult<T>): DomainResult<T> =
        try {
            block()
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }

    private fun AssetRoleDto.toDomain() = AssetRoleInfo(
        id = id,
        personId = personId,
        isMol = role == "mol",
        ackStatus = AckStatus.fromServer(ackStatus),
        ackReason = ackReason,
    )

    private fun AssetDetailDto.toDomain() = AssetServerInfo(
        uuid = uuid,
        name = name.orEmpty(),
        inventoryNum = inventoryNum,
        accountingStatus = accountingStatus,
        condition = condition,
        moderationStatus = moderationStatus,
        mol = mol?.toDomain(),
        users = users.orEmpty().map { it.toDomain() },
        inRepairOverdue = openRepair?.overdue == true,
    )

    override suspend fun getAssetInfo(serverUuid: String): DomainResult<AssetServerInfo> = call {
        val response = api.getAsset(serverUuid)
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        val body = response.body() ?: return@call DomainResult.ValidationError("Актив не найден")
        DomainResult.Success(body.toDomain())
    }

    override suspend fun myAssignments(): DomainResult<List<Assignment>> = call {
        val response = api.myAssignments()
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        DomainResult.Success(
            response.body().orEmpty().map { dto ->
                Assignment(
                    role = AssetRoleInfo(
                        id = dto.id,
                        personId = dto.personId,
                        isMol = dto.role == "mol",
                        ackStatus = AckStatus.fromServer(dto.ackStatus),
                        ackReason = dto.ackReason,
                    ),
                    asset = dto.asset?.toDomain(),
                )
            }
        )
    }

    override suspend fun ack(roleId: String, accept: Boolean, reason: String?): DomainResult<Unit> = call {
        val response = api.ack(roleId, AckRequest(if (accept) "accept" else "dispute", reason?.takeIf { it.isNotBlank() }))
        if (!response.isSuccessful) responseErrorMapper.map(response) else DomainResult.Success(Unit)
    }

    override suspend fun transferMol(serverUuid: String, personId: String, comment: String?): DomainResult<Unit> = call {
        val response = api.assignMol(serverUuid, AssignPersonRequest(personId, comment?.takeIf { it.isNotBlank() }))
        if (!response.isSuccessful) responseErrorMapper.map(response) else DomainResult.Success(Unit)
    }

    override suspend fun addUser(serverUuid: String, personId: String): DomainResult<Unit> = call {
        val response = api.addUser(serverUuid, AssignPersonRequest(personId))
        if (!response.isSuccessful) responseErrorMapper.map(response) else DomainResult.Success(Unit)
    }

    override suspend fun reportFault(serverUuid: String, message: String): DomainResult<Unit> = call {
        val response = api.createRepairRequest(CreateRepairRequestDto(serverUuid, message.trim(), markFaulty = true))
        if (!response.isSuccessful) responseErrorMapper.map(response) else DomainResult.Success(Unit)
    }
}
