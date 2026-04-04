import com.samyak.repostore.data.model.*
import com.samyak.gitcore.util.IconResolver

fun main() {
    // Mock GitHubRepo for Simple-Tube
    val repo = GitHubRepo(
        id = 1,
        name = "Simple-Tube",
        fullName = "samyak2403/Simple-Tube",
        description = "A simple YouTube client",
        htmlUrl = "https://github.com/samyak2403/Simple-Tube",
        stars = 100,
        forks = 10,
        language = "Kotlin",
        updatedAt = "2024-03-24T00:00:00Z",
        createdAt = "2023-01-01T00:00:00Z",
        archived = false,
        owner = GitHubRepo.Owner(
            login = "samyak2403",
            avatarUrl = "https://avatars.githubusercontent.com/u/1?v=4",
            htmlUrl = "https://github.com/samyak2403"
        ),
        topics = listOf("android", "youtube"),
        defaultBranch = "main"
    )

    val icons = IconResolver.resolve(repo.owner.login, repo.name, repo.defaultBranch, repo.language)
    println("Resolved Icons for ${repo.fullName}:")
    icons.forEachIndexed { index, url ->
        println("${index + 1}. $url")
    }
}
