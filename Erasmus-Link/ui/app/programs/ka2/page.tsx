import Link from "next/link"
import Image from "next/image"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Star, MapPin, Calendar, Building, Users2, Lightbulb } from 'lucide-react'

export default function KA2ProgramsPage() {
  // Sample KA2 programs data
  const programs = [
    {
      id: 2,
      title: "Innovation in Education",
      organization: "Berlin Institute of Technology",
      location: "Berlin, Germany",
      rating: 4.5,
      reviews: 18,
      startDate: "2025-10-15",
      duration: "6 months",
      description: "A collaborative project focused on developing innovative teaching methods in technical education.",
      tags: ["Innovation", "Education", "Technology"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 5,
      title: "Entrepreneurship Bootcamp",
      organization: "Lisbon Business School",
      location: "Lisbon, Portugal",
      rating: 4.6,
      reviews: 27,
      startDate: "2025-07-15",
      duration: "2 months",
      description: "Intensive bootcamp for aspiring entrepreneurs to develop business ideas and startup skills.",
      tags: ["Entrepreneurship", "Business", "Innovation"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 10,
      title: "Digital Education Consortium",
      organization: "Multiple European Universities",
      location: "Various Locations",
      rating: 4.7,
      reviews: 22,
      startDate: "2025-09-01",
      duration: "24 months",
      description:
        "A strategic partnership between universities to develop innovative digital education tools and methodologies.",
      tags: ["Digital", "Education", "Collaboration"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 11,
      title: "Sustainable Agriculture Network",
      organization: "Agricultural University of Athens",
      location: "Multiple Countries",
      rating: 4.8,
      reviews: 15,
      startDate: "2025-11-01",
      duration: "36 months",
      description:
        "A collaborative project to develop and share sustainable agricultural practices across European countries.",
      tags: ["Agriculture", "Sustainability", "Research"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 12,
      title: "Inclusive Education Alliance",
      organization: "University of Copenhagen",
      location: "Copenhagen, Denmark",
      rating: 4.9,
      reviews: 24,
      startDate: "2025-10-01",
      duration: "30 months",
      description:
        "A strategic partnership focused on developing inclusive education methodologies for students with disabilities.",
      tags: ["Inclusion", "Education", "Accessibility"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 13,
      title: "Creative Industries Collaboration",
      organization: "Royal Academy of Arts",
      location: "Amsterdam, Netherlands",
      rating: 4.6,
      reviews: 19,
      startDate: "2025-09-15",
      duration: "24 months",
      description:
        "A partnership between arts institutions to foster innovation and entrepreneurship in creative industries.",
      tags: ["Creative", "Arts", "Entrepreneurship"],
      image: "/placeholder.svg?height=200&width=400",
    },
  ]

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero Section */}
        <section className="relative">
          <div className="absolute inset-0 z-0">
            <Image
              src="/placeholder.svg?height=400&width=1200"
              alt="Collaboration meeting"
              fill
              className="object-cover brightness-50"
              priority
            />
          </div>
          <div className="relative z-10 flex min-h-[400px] items-center justify-center px-4 text-center text-white">
            <div className="max-w-3xl">
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">KA2 Programs</h1>
              <p className="mt-6 text-xl">Cooperation for Innovation</p>
              <p className="mt-4 text-lg">
                KA2 supports institutional cooperation between organizations to develop innovative outputs and share best
                practices across sectors.
              </p>
            </div>
          </div>
        </section>

        {/* Info Section */}
        <section className="bg-muted/50 py-12">
          <div className="container px-4 md:px-6">
            <div className="grid gap-6 md:grid-cols-3">
              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <Building className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Strategic Partnerships</CardTitle>
                    <CardDescription>Cooperation between organizations</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Organizations work together to develop innovative approaches, exchange best practices, and create
                    high-quality outputs.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <Lightbulb className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Innovation</CardTitle>
                    <CardDescription>Developing new methodologies and tools</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    KA2 projects focus on creating innovative outputs that can be shared and implemented across sectors.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <Users2 className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Knowledge Alliances</CardTitle>
                    <CardDescription>Cooperation between education and business</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Bringing together higher education institutions and businesses to foster innovation and
                    entrepreneurship.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* Programs Section */}
        <section className="py-12">
          <div className="container px-4 md:px-6">
            <h2 className="mb-8 text-3xl font-bold tracking-tight">Featured KA2 Programs</h2>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {programs.map((program) => (
                <Link href={`/programs/${program.id}`} key={program.id}>
                  <Card className="h-full overflow-hidden transition-all hover:shadow-md">
                    <div className="aspect-video w-full overflow-hidden">
                      <Image
                        src={program.image || "/placeholder.svg"}
                        alt={program.title}
                        width={400}
                        height={200}
                        className="h-full w-full object-cover transition-transform hover:scale-105"
                      />
                    </div>
                    <CardHeader className="pb-2">
                      <div className="flex justify-between">
                        <Badge>KA2</Badge>
                        <div className="flex items-center">
                          <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                          <span className="text-sm font-medium">
                            {program.rating} ({program.reviews})
                          </span>
                        </div>
                      </div>
                      <CardTitle className="line-clamp-1">{program.title}</CardTitle>
                      <CardDescription className="line-clamp-1">{program.organization}</CardDescription>
                    </CardHeader>
                    <CardContent className="pb-2">
                      <div className="flex items-center text-sm text-muted-foreground">
                        <MapPin className="mr-1 h-4 w-4" />
                        {program.location}
                      </div>
                      <div className="mt-2 flex items-center text-sm text-muted-foreground">
                        <Calendar className="mr-1 h-4 w-4" />
                        Starts: {new Date(program.startDate).toLocaleDateString()} • {program.duration}
                      </div>
                      <p className="mt-2 line-clamp-2 text-sm">{program.description}</p>
                    </CardContent>
                    <CardFooter className="flex flex-wrap gap-1">
                      {program.tags.map((tag) => (
                        <Badge key={tag} variant="outline" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </CardFooter>
                  </Card>
                </Link>
              ))}
            </div>
            <div className="mt-8 flex justify-center">
              <Button asChild>
                <Link href="/programs">View All Programs</Link>
              </Button>
            </div>
          </div>
        </section>

        {/* Project Types Section */}
        <section className="bg-muted/30 py-12">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl text-center">
              <h2 className="text-3xl font-bold tracking-tight">Types of KA2 Projects</h2>
              <p className="mt-4 text-muted-foreground">
                KA2 encompasses various types of collaborative projects across different sectors
              </p>
            </div>
            <div className="mt-8 grid gap-6 md:grid-cols-2">
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Strategic Partnerships</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Organizations from different countries work together to develop innovative approaches and exchange
                    best practices. These partnerships can focus on education, training, youth, or multiple sectors.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Knowledge Alliances</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Transnational projects between higher education institutions and businesses that foster innovation,
                    entrepreneurship, and knowledge exchange.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Sector Skills Alliances</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Projects that address skills gaps in specific economic sectors by developing vocational training
                    curricula and innovative training methodologies.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Capacity Building Projects</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Collaborative projects between organizations from Program Countries and Partner Countries aimed at
                    supporting modernization and internationalization in higher education and youth sectors.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-12">
          <div className="container px-4 md:px-6">
            <div className="rounded-lg bg-primary/10 p-8 text-center">
              <h2 className="text-2xl font-bold tracking-tight">Interested in Cooperation Projects?</h2>
              <p className="mt-4 text-muted-foreground">
                Explore KA2 programs and find opportunities for your organization to collaborate on innovative projects.
              </p>
              <div className="mt-6 flex flex-col justify-center gap-4 sm:flex-row">
                <Button asChild size="lg">
                  <Link href="/register">Register Your Organization</Link>
                </Button>
                <Button asChild variant="outline" size="lg">
                  <Link href="/programs">Browse All Programs</Link>
                </Button>
              </div>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}
