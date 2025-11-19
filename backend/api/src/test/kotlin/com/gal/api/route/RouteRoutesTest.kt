package com.gal.api.route

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import com.gal.core.route.Route
import com.gal.persistence.routes.RouteRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class RouteRoutesTest {

    @BeforeTest
    fun setup() {
        // Clear repository before each test
        RouteRepositoryLocator.setRouteRepository(StubRouteRepository())
    }

    @AfterTest
    fun teardown() {
        RouteRepositoryLocator.setRouteRepository(StubRouteRepository())
    }

    @Test
    fun testGetRoutesWithoutDatabase() = testApplication {
        RouteRepositoryLocator.clear()
        
        val response = client.get("/api/routes")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("service_unavailable", errorResponse.error.code)
    }

    @Test
    fun testGetRoutesEmpty() = testApplication {
        val response = client.get("/api/routes")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val routes = Json.decodeFromString<List<RouteResponse>>(body)
        assertEquals(0, routes.size)
    }

    @Test
    fun testGetRoutesByAirline() = testApplication {
        val stubRepo = StubRouteRepository()
        stubRepo.addRoute(createTestRoute(1, 1, 1, 2))
        stubRepo.addRoute(createTestRoute(2, 1, 2, 3))
        stubRepo.addRoute(createTestRoute(3, 2, 1, 3))
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.get("/api/routes?airlineId=1")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val routes = Json.decodeFromString<List<RouteResponse>>(body)
        assertEquals(2, routes.size)
        assertTrue(routes.all { it.airlineId == 1L })
    }

    @Test
    fun testGetRoutesByAirlineInvalidId() = testApplication {
        val response = client.get("/api/routes?airlineId=-1")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testGetRouteById() = testApplication {
        val stubRepo = StubRouteRepository()
        val route = createTestRoute(1, 1, 1, 2)
        stubRepo.addRoute(route)
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.get("/api/routes/1")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val routeResponse = Json.decodeFromString<RouteResponse>(body)
        assertEquals(1L, routeResponse.id)
        assertEquals(1L, routeResponse.airlineId)
    }

    @Test
    fun testGetRouteByIdNotFound() = testApplication {
        val response = client.get("/api/routes/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRouteByIdInvalid() = testApplication {
        val response = client.get("/api/routes/invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRoute() = testApplication {
        val stubRepo = StubRouteRepository()
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.post("/api/routes") {
            contentType(ContentType.Application.Json)
            setBody("""{"airlineId":1,"originAirportId":1,"destinationAirportId":2}""")
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        
        val body = response.bodyAsText()
        val routeResponse = Json.decodeFromString<RouteResponse>(body)
        assertEquals(1L, routeResponse.airlineId)
        assertEquals(1L, routeResponse.originAirportId)
        assertEquals(2L, routeResponse.destinationAirportId)
        assertTrue(routeResponse.distanceKm > 0)
    }

    @Test
    fun testCreateRouteSameOriginDestination() = testApplication {
        val response = client.post("/api/routes") {
            contentType(ContentType.Application.Json)
            setBody("""{"airlineId":1,"originAirportId":1,"destinationAirportId":1}""")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("invalid_input", errorResponse.error.code)
    }

    @Test
    fun testCreateRouteInvalidIds() = testApplication {
        val response = client.post("/api/routes") {
            contentType(ContentType.Application.Json)
            setBody("""{"airlineId":-1,"originAirportId":1,"destinationAirportId":2}""")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRouteDuplicate() = testApplication {
        val stubRepo = StubRouteRepository()
        stubRepo.shouldReturnDuplicate = true
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.post("/api/routes") {
            contentType(ContentType.Application.Json)
            setBody("""{"airlineId":1,"originAirportId":1,"destinationAirportId":2}""")
        }
        
        assertEquals(HttpStatusCode.Conflict, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("duplicate_route", errorResponse.error.code)
    }

    @Test
    fun testCreateRouteAirlineNotFound() = testApplication {
        val stubRepo = StubRouteRepository()
        stubRepo.shouldReturnAirlineNotFound = true
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.post("/api/routes") {
            contentType(ContentType.Application.Json)
            setBody("""{"airlineId":999,"originAirportId":1,"destinationAirportId":2}""")
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("not_found", errorResponse.error.code)
    }

    @Test
    fun testDeleteRoute() = testApplication {
        val stubRepo = StubRouteRepository()
        stubRepo.addRoute(createTestRoute(1, 1, 1, 2))
        RouteRepositoryLocator.setRouteRepository(stubRepo)
        
        val response = client.delete("/api/routes/1")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun testDeleteRouteNotFound() = testApplication {
        val response = client.delete("/api/routes/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRoutesPagination() = testApplication {
        val response = client.get("/api/routes?offset=0&limit=50")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetRoutesInvalidPagination() = testApplication {
        val response = client.get("/api/routes?offset=-1&limit=50")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun createTestRoute(
        id: Long,
        airlineId: Long,
        originId: Long,
        destId: Long
    ): Route {
        return Route(
            id = RouteId(id),
            airlineId = AirlineId(airlineId),
            originAirportId = AirportId(originId),
            destinationAirportId = AirportId(destId),
            distanceKm = 1000,
            createdAtEpochSeconds = System.currentTimeMillis() / 1000
        )
    }
}

/**
 * Stub repository for testing routes.
 */
class StubRouteRepository : RouteRepository {
    private val routes = mutableMapOf<Long, Route>()
    private var nextId = 1L
    var shouldReturnDuplicate = false
    var shouldReturnAirlineNotFound = false
    var shouldReturnAirportNotFound = false

    fun addRoute(route: Route) {
        routes[route.id.value] = route
    }

    override suspend fun create(
        airlineId: AirlineId,
        originAirportId: AirportId,
        destinationAirportId: AirportId
    ): Result<Route> {
        if (shouldReturnDuplicate) {
            return Result.failure(IllegalArgumentException("Route already exists for this airline"))
        }
        if (shouldReturnAirlineNotFound) {
            return Result.failure(IllegalArgumentException("Airline not found"))
        }
        if (shouldReturnAirportNotFound) {
            return Result.failure(IllegalArgumentException("Origin airport not found"))
        }

        val route = Route(
            id = RouteId(nextId++),
            airlineId = airlineId,
            originAirportId = originAirportId,
            destinationAirportId = destinationAirportId,
            distanceKm = 1000,
            createdAtEpochSeconds = System.currentTimeMillis() / 1000
        )
        routes[route.id.value] = route
        return Result.success(route)
    }

    override suspend fun get(id: RouteId): Route? {
        return routes[id.value]
    }

    override suspend fun listByAirline(
        airlineId: AirlineId,
        offset: Int,
        limit: Int
    ): List<Route> {
        return routes.values.filter { it.airlineId == airlineId }
            .drop(offset)
            .take(limit)
    }

    override suspend fun delete(id: RouteId): Boolean {
        return routes.remove(id.value) != null
    }

    override suspend fun listAll(offset: Int, limit: Int): List<Route> {
        return routes.values.drop(offset).take(limit)
    }

    override suspend fun listByAirport(
        airportId: AirportId,
        offset: Int,
        limit: Int
    ): List<Route> {
        return routes.values
            .filter { it.originAirportId == airportId || it.destinationAirportId == airportId }
            .drop(offset)
            .take(limit)
    }
}
