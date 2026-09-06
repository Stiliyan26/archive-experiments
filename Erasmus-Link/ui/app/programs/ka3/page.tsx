import Link from "next/link"
import Image from "next/image"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Star, MapPin, Calendar, FileText, Globe, BookOpen } from "lucide-react"

export default function KA3ProgramsPage() {
  // Sample KA3 programs data
  const programs = [
    {
      id: 4,
      title: "Sustainable Development Research",
      organization: "University of Copenhagen",
      location: "Copenhagen, Denmark",
      rating: 4.7,
      reviews: 15,
      startDate: "2025-11-01",
      duration: "12 months",
      description: "Research program focused on sustainable development policies and practices across Europe.",
      tags: ["Research", "Sustainability", "Policy"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 14,
      title: "Educational Policy Reform Initiative",
      organization: "European Policy Institute",
      location: "Brussels, Belgium",
      rating: 4.8,
      reviews: 22,
      startDate: "2025-09-01",
      duration: "24 months",
      description: "A collaborative project to develop policy recommendations for educational reform across Europe.",
      tags: ["Policy", "Education", "Reform"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 15,
      title: "Youth Participation in Democracy",
      organization: "European Youth Forum",
      location: "Multiple Countries",
      rating: 4.6,
      reviews: 18,
      startDate: "2025-10-15",
      duration: "18 months",
      description: "A project focused on increasing youth participation in democratic processes and policy-making.",
      tags: ["Youth", "Democracy", "Participation"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 16,
      title: "Digital Skills Policy Framework",
      organization: "Digital Europe Association",
      location: "Various Locations",
      rating: 4.9,
      reviews: 24,
      startDate: "2025-09-15",
      duration: "30 months",
      description:
        "Developing a comprehensive policy framework for digital skills education across European countries.",
      tags: ["Digital", "Policy", "Education"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 17,
      title: "Higher Education Modernization",
      organization: "European University Association",
      location: "Multiple Countries",
      rating: 4.7,
      reviews: 20,
      startDate: "2025-11-01",
      duration: "36 months",
      description: "A large-scale project to support policy reform for modernizing higher education systems in Europe.",
      tags: ["Higher Education", "Modernization", "Reform"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 18,
      title: "Inclusive Education Policy Network",
      organization: "European Agency for Special Needs",
      location: "Multiple Countries",
      rating: 4.8,
      reviews: 16,
      startDate: "2025-10-01",
      duration: "24 months",
      description: "A network focused on developing inclusive education policies for students with special needs.",
      tags: ["Inclusion", "Policy", "Special Needs"],
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
              alt="Policy discussion"
              fill
              className="object-cover brightness-50"
              priority
            />
          </div>
          <div className="relative z-10 flex min-h-[400px] items-center justify-center px-4 text-center text-white">
            <div className="max-w-3xl">
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">KA3 Programs</h1>
              <p className="mt-6 text-xl">Support for Policy Reform</p>
              <p className="mt-4 text-lg">
                KA3 supports policy reform initiatives aimed at strengthening education, training, and youth systems
                across Europe.
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
                  <FileText className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Policy Development</CardTitle>
                    <CardDescription>Supporting evidence-based policy reform</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    KA3 projects contribute to the development of evidence-based policies that strengthen education and
                    training systems.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <Globe className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>European Cooperation</CardTitle>
                    <CardDescription>Fostering dialogue between stakeholders</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    These initiatives bring together policymakers, institutions, and stakeholders to address common
                    challenges.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <BookOpen className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Knowledge Exchange</CardTitle>
                    <CardDescription>Sharing best practices in policy implementation</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    KA3 facilitates the exchange of knowledge and best practices in policy development and
                    implementation.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* Programs Section */}
        <section className="py-12">
          <div className="container px-4 md:px-6">
            <h2 className="mb-8 text-3xl font-bold tracking-tight">Featured KA3 Programs</h2>
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
                        <Badge>KA3</Badge>
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

        {/* Policy Areas Section */}
        <section className="bg-muted/30 py-12">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl text-center">
              <h2 className="text-3xl font-bold tracking-tight">Key Policy Areas</h2>
              <p className="mt-4 text-muted-foreground">
                KA3 focuses on several key policy areas to strengthen education and training systems
              </p>
            </div>
            <div className="mt-8 grid gap-6 md:grid-cols-2">
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Education and Training Reforms</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Supporting the modernization of education and training systems through policy development and
                    implementation.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Youth Participation</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Promoting the active participation of young people in democratic life and policy-making processes.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Digital Education</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Developing policies to enhance digital skills and competencies across education and training
                    systems.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle>Inclusive Education</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Creating policies that ensure equal access to quality education for all learners, regardless of
                    their background or abilities.
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
              <h2 className="text-2xl font-bold tracking-tight">Contribute to Policy Reform</h2>
              <p className="mt-4 text-muted-foreground">
                Explore KA3 programs and find opportunities to contribute to policy development and implementation in
                education and training.
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
