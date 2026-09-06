import Link from "next/link"
import Image from "next/image"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Star, MapPin, Calendar, Users, GraduationCap, Globe } from 'lucide-react'

export default function KA1ProgramsPage() {
  // Sample KA1 programs data
  const programs = [
    {
      id: 1,
      title: "Cultural Exchange Program",
      organization: "University of Barcelona",
      location: "Barcelona, Spain",
      rating: 4.8,
      reviews: 24,
      startDate: "2025-09-01",
      duration: "10 months",
      description:
        "Join our cultural exchange program and experience life in Barcelona while studying at one of Spain's top universities.",
      tags: ["Cultural", "Language", "Academic"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 3,
      title: "Digital Skills Workshop",
      organization: "University of Helsinki",
      location: "Helsinki, Finland",
      rating: 4.9,
      reviews: 32,
      startDate: "2025-08-01",
      duration: "3 months",
      description: "Enhance your digital skills in this intensive workshop program at the University of Helsinki.",
      tags: ["Digital", "Skills", "Workshop"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 6,
      title: "Arts and Culture Exchange",
      organization: "Academy of Fine Arts Vienna",
      location: "Vienna, Austria",
      rating: 4.4,
      reviews: 19,
      startDate: "2025-09-15",
      duration: "5 months",
      description:
        "Immerse yourself in the rich cultural heritage of Vienna while studying at the prestigious Academy of Fine Arts.",
      tags: ["Arts", "Culture", "Creative"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 7,
      title: "Language Teaching Assistantship",
      organization: "University of Warsaw",
      location: "Warsaw, Poland",
      rating: 4.6,
      reviews: 15,
      startDate: "2025-10-01",
      duration: "9 months",
      description:
        "Work as a language teaching assistant while experiencing Polish culture and improving your teaching skills.",
      tags: ["Teaching", "Language", "Education"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 8,
      title: "Healthcare Internship Program",
      organization: "Karolinska Institute",
      location: "Stockholm, Sweden",
      rating: 4.9,
      reviews: 28,
      startDate: "2025-09-01",
      duration: "6 months",
      description: "Gain practical experience in healthcare settings at one of Europe's leading medical universities.",
      tags: ["Healthcare", "Internship", "Medical"],
      image: "/placeholder.svg?height=200&width=400",
    },
    {
      id: 9,
      title: "Youth Leadership Exchange",
      organization: "European Youth Foundation",
      location: "Multiple Locations",
      rating: 4.7,
      reviews: 31,
      startDate: "2025-07-15",
      duration: "2 weeks",
      description:
        "A short-term intensive program focused on developing leadership skills among young people across Europe.",
      tags: ["Leadership", "Youth", "Short-term"],
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
              alt="Students in a classroom"
              fill
              className="object-cover brightness-50"
              priority
            />
          </div>
          <div className="relative z-10 flex min-h-[400px] items-center justify-center px-4 text-center text-white">
            <div className="max-w-3xl">
              <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">KA1 Programs</h1>
              <p className="mt-6 text-xl">Learning Mobility of Individuals</p>
              <p className="mt-4 text-lg">
                KA1 supports mobility of learners and staff, Erasmus Mundus Joint Master Degrees, Erasmus+ Master Loans
                and other international initiatives.
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
                  <Users className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>For Individuals</CardTitle>
                    <CardDescription>Students, trainees, apprentices, pupils, adult learners</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    KA1 provides opportunities for individuals to improve their skills, enhance their employability and
                    gain cultural awareness.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <GraduationCap className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>For Staff</CardTitle>
                    <CardDescription>Teachers, trainers, youth workers, educational staff</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Staff can engage in professional development activities abroad, including teaching, training, and job
                    shadowing.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center gap-4">
                  <Globe className="h-8 w-8 text-primary" />
                  <div>
                    <CardTitle>Global Reach</CardTitle>
                    <CardDescription>Mobility between Program and Partner Countries</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    KA1 supports international mobility activities involving partner countries across the world.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* Programs Section */}
        <section className="py-12">
          <div className="container px-4 md:px-6">
            <h2 className="mb-8 text-3xl font-bold tracking-tight">Featured KA1 Programs</h2>
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
                        <Badge>KA1</Badge>
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

        {/* Benefits Section */}
        <section className="bg-muted/30 py-12">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl text-center">
              <h2 className="text-3xl font-bold tracking-tight">Benefits of KA1 Mobility</h2>
              <p className="mt-4 text-muted-foreground">
                Learning mobility contributes to personal and professional development in multiple ways
              </p>
            </div>
            <div className="mt-8 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle className="text-center">Improved Language Skills</CardTitle>
                </CardHeader>
                <CardContent className="text-center">
                  <p className="text-muted-foreground">
                    Immerse yourself in a new language environment and significantly enhance your language proficiency.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle className="text-center">Cultural Awareness</CardTitle>
                </CardHeader>
                <CardContent className="text-center">
                  <p className="text-muted-foreground">
                    Develop a deeper understanding of different cultures, traditions, and perspectives.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle className="text-center">Enhanced Employability</CardTitle>
                </CardHeader>
                <CardContent className="text-center">
                  <p className="text-muted-foreground">
                    Gain valuable skills and international experience that make you stand out to employers.
                  </p>
                </CardContent>
              </Card>
              <Card className="bg-background">
                <CardHeader>
                  <CardTitle className="text-center">Personal Growth</CardTitle>
                </CardHeader>
                <CardContent className="text-center">
                  <p className="text-muted-foreground">
                    Build confidence, independence, and adaptability by navigating new environments.
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
              <h2 className="text-2xl font-bold tracking-tight">Ready to Start Your Mobility Journey?</h2>
              <p className="mt-4 text-muted-foreground">
                Explore KA1 programs and find the perfect opportunity for your learning mobility experience.
              </p>
              <div className="mt-6 flex flex-col justify-center gap-4 sm:flex-row">
                <Button asChild size="lg">
                  <Link href="/register">Create an Account</Link>
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
