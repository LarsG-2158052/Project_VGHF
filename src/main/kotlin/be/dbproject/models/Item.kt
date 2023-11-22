package be.dbproject.models

import javax.persistence.*

@Entity
@Table(name = "item")
data class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val typeId: Int,
    val platformId: Int?,
    val locationId: Int,
    val publisherId: Int?,
    val name: String,
    val price: Double,
    val description: String,
    val series: String,
    val releaseDate: String
) {
    constructor() : this(0, 0, null, 0, null, "", 0.0, "", "", "")
}