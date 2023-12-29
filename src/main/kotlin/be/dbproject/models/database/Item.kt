package be.dbproject.models.database

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "Item")
data class Item(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "price", nullable = false)
    var price: Double,

    @Column(name = "description", nullable = false)
    var description: String,

    @Column(name = "series", nullable = false)
    var series: String,

    @ManyToOne
    @JoinColumn(name = "typeid", nullable = false)
    var itemType: ItemType,

    @ManyToOne
    @JoinColumn(name = "platformid")
    var platform: Platform? = null,

    @ManyToOne
    @JoinColumn(name = "locationid", nullable = false)
    var location: Location,

    @ManyToOne
    @JoinColumn(name = "publisherid")
    var publisher: Publisher? = null,

    @Column(name = "releaseDate", nullable = false)
    var releaseDate: LocalDate,

    @ManyToMany
    @JoinTable(
        name = "ItemGenreLink",
        joinColumns = [JoinColumn(name = "itemid")],
        inverseJoinColumns = [JoinColumn(name = "genreid")]
    )
    var genres: Set<Genre>
) : DatabaseModel() {
    constructor() :this(
        "",
        0.0,
        "",
        "",
        ItemType(),
        Platform(),
        Location(),
        null,
        LocalDate.now(),
        HashSet<Genre>()
    )

    override fun toString(): String {
        return name
    }
}