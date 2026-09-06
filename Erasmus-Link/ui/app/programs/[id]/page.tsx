"use client"

import { useState, useEffect } from "react"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Star, MapPin, Calendar, Building, Clock, MessageSquare, Share2, Bookmark, Flag } from "lucide-react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Separator } from "@/components/ui/separator"
import { Textarea } from "@/components/ui/textarea"
import { Progress } from "@/components/ui/progress"
import { useParams } from "next/navigation"

// Define types for our program data
interface Review {
  id: number
  user: string
  avatar: string
  rating: number
  date: string
  comment: string
}

interface Program {
  id: number
  title: string
  organization: string
  location: string
  type: string
  rating: number
  reviews: Review[]
  startDate: string
  endDate: string
  duration: string
  description: string
  tags: string[]
  requirements: string[]
  benefits: string[]
  applicationDeadline: string
  contactPerson: string
  contactEmail: string
  website: string
}

// Sample program data
const programs: Program[] = [
  {
    id: 1,
    title: "Cultural Exchange Program",
    organization: "University of Barcelona",
    location: "Barcelona, Spain",
    type: "KA1",
    rating: 4.8,
    reviews: [
      {
        id: 1,
        user: "Sophie K.",
        avatar: "/placeholder.svg?height=40&width=40",
        rating: 5,
        date: "2024-01-15",
        comment:
          "My semester at UB was incredible! The professors were knowledgeable and supportive, and Barcelona is an amazing city to live in. Highly recommend this program!",
      },
      {
        id: 2,
        user: "Thomas M.",
        avatar: "/placeholder.svg?height=40&width=40",
        rating: 4,
        date: "2023-12-10",
        comment:
          "Great academic experience and wonderful city. The only challenge was finding affordable housing, but the university provided good support.",
      },
      {
        id: 3,
        user: "Elena P.",
        avatar: "/placeholder.svg?height=40&width=40",
        rating: 5,
        date: "2023-11-22",
        comment:
          "This program changed my life! I improved my Spanish significantly, made friends from all over the world, and the cultural activities were fantastic.",
      },
    ],
    startDate: "2025-09-01",
    endDate: "2026-06-30",
    duration: "10 months",
    description:
      "Join our cultural exchange program and experience life in Barcelona while studying at one of Spain's top universities. This program offers a unique opportunity to immerse yourself in Spanish culture, improve your language skills, and gain international academic experience.\n\nParticipants will attend regular classes at the University of Barcelona, participate in cultural activities, and have the opportunity to explore the rich history and vibrant lifestyle of Catalonia. Housing options include student dormitories or homestays with local families.",
    tags: ["Cultural", "Language", "Academic"],
    requirements: [
      "Currently enrolled in a higher education institution",
      "Intermediate level of Spanish or Catalan",
      "Good academic standing",
      "Letter of motivation",
    ],
    benefits: [
      "Monthly stipend for living expenses",
      "Accommodation assistance",
      "Language courses",
      "Cultural activities and excursions",
      "Academic credits recognition",
    ],
    applicationDeadline: "2025-03-15",
    contactPerson: "Dr. Maria Rodriguez",
    contactEmail: "erasmus@ub.edu",
    website: "https://www.ub.edu/erasmus",
  },
  {
    id: 2,
    title: "Innovation in Education",
    organization: "Berlin Institute of Technology",
    location: "Berlin, Germany",
    type: "KA2",
    rating: 4.5,
    reviews: [],
    startDate: "2025-10-15",
    endDate: "2026-04-15",
    duration: "6 months",
    description: "A collaborative project focused on developing innovative teaching methods in technical education.",
    tags: ["Innovation", "Education", "Technology"],
    requirements: [
      "Background in education or technology",
      "Interest in educational innovation",
      "Good communication skills",
    ],
    benefits: [
      "Project-based learning experience",
      "Networking with education professionals",
      "Certificate of participation",
    ],
    applicationDeadline: "2025-05-30",
    contactPerson: "Prof. Hans Schmidt",
    contactEmail: "erasmus@bit.de",
    website: "https://www.bit.de/erasmus",
  },
  {
    id: 4,
    title: "Sustainable Development Research",
    organization: "University of Copenhagen",
    location: "Copenhagen, Denmark",
    type: "KA3",
    rating: 4.7,
    reviews: [],
    startDate: "2025-11-01",
    endDate: "2026-10-31",
    duration: "12 months",
    description: "Research program focused on sustainable development policies and practices across Europe.",
    tags: ["Research", "Sustainability", "Policy"],
    requirements: ["Master's degree in a relevant field", "Research experience", "Interest in sustainability"],
    benefits: ["Research stipend", "Conference participation", "Publication opportunities"],
    applicationDeadline: "2025-06-30",
    contactPerson: "Dr. Lars Nielsen",
    contactEmail: "erasmus@ku.dk",
    website: "https://www.ku.dk/erasmus",
  },
]

export default function ProgramDetailPage() {
  const params = useParams()
  const [program, setProgram] = useState<Program | null>(null)
  const [newReview, setNewReview] = useState("")
  const [newRating, setNewRating] = useState(5)

  useEffect(() => {
    if (params.id) {
      const programId = Number(params.id)
      const foundProgram = programs.find((p) => p.id === programId)
      if (foundProgram) {
        setProgram(foundProgram)
      }
    }
  }, [params.id])

  if (!program) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <h1 className="text-2xl font-bold">Loading program...</h1>
            <p className="mt-2 text-muted-foreground">Please wait while we fetch the program details.</p>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  const handleSubmitReview = () => {
    // In a real application, this would send the review to the backend
    alert("Review submitted successfully!")
    setNewReview("")
  }

  // Calculate rating distribution
  const ratingDistribution = [
    { stars: 5, count: program.reviews.filter((r) => r.rating === 5).length },
    { stars: 4, count: program.reviews.filter((r) => r.rating === 4).length },
    { stars: 3, count: program.reviews.filter((r) => r.rating === 3).length },
    { stars: 2, count: program.reviews.filter((r) => r.rating === 2).length },
    { stars: 1, count: program.reviews.filter((r) => r.rating === 1).length },
  ]

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <div className="container px-4 py-8 md:px-6">
          <div className="mb-6 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <div className="flex items-center gap-2">
                <Badge>{program.type}</Badge>
                <div className="flex items-center">
                  <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                  <span className="text-sm font-medium">
                    {program.rating} ({program.reviews.length} reviews)
                  </span>
                </div>
              </div>
              <h1 className="mt-2 text-3xl font-bold tracking-tight md:text-4xl">{program.title}</h1>
              <div className="mt-2 flex flex-col gap-2 md:flex-row md:items-center md:gap-4">
                <div className="flex items-center text-muted-foreground">
                  <Building className="mr-1 h-4 w-4" />
                  {program.organization}
                </div>
                <div className="flex items-center text-muted-foreground">
                  <MapPin className="mr-1 h-4 w-4" />
                  {program.location}
                </div>
                <div className="flex items-center text-muted-foreground">
                  <Calendar className="mr-1 h-4 w-4" />
                  {new Date(program.startDate).toLocaleDateString()} - {new Date(program.endDate).toLocaleDateString()}
                </div>
                <div className="flex items-center text-muted-foreground">
                  <Clock className="mr-1 h-4 w-4" />
                  {program.duration}
                </div>
              </div>
            </div>
            <div className="mt-4 flex gap-2 md:mt-0">
              <Button variant="outline" size="sm" className="gap-1">
                <Share2 className="h-4 w-4" />
                Share
              </Button>
              <Button variant="outline" size="sm" className="gap-1">
                <Bookmark className="h-4 w-4" />
                Save
              </Button>
              <Button size="sm">Apply Now</Button>
            </div>
          </div>

          <div className="grid gap-6 md:grid-cols-3">
            <div className="md:col-span-2">
              <Tabs defaultValue="overview">
                <TabsList className="mb-4">
                  <TabsTrigger value="overview">Overview</TabsTrigger>
                  <TabsTrigger value="requirements">Requirements</TabsTrigger>
                  <TabsTrigger value="benefits">Benefits</TabsTrigger>
                  <TabsTrigger value="reviews">Reviews</TabsTrigger>
                </TabsList>
                <TabsContent value="overview" className="space-y-4">
                  <Card>
                    <CardHeader>
                      <CardTitle>Program Description</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        {program.description.split("\n\n").map((paragraph, i) => (
                          <p key={i}>{paragraph}</p>
                        ))}
                      </div>
                      <div className="mt-4 flex flex-wrap gap-2">
                        {program.tags.map((tag) => (
                          <Badge key={tag} variant="outline">
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
                <TabsContent value="requirements" className="space-y-4">
                  <Card>
                    <CardHeader>
                      <CardTitle>Program Requirements</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="ml-6 list-disc space-y-2">
                        {program.requirements.map((req, i) => (
                          <li key={i}>{req}</li>
                        ))}
                      </ul>
                      <div className="mt-6">
                        <h4 className="font-medium">Application Deadline</h4>
                        <p className="mt-1 flex items-center text-muted-foreground">
                          <Calendar className="mr-2 h-4 w-4" />
                          {new Date(program.applicationDeadline).toLocaleDateString()}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
                <TabsContent value="benefits" className="space-y-4">
                  <Card>
                    <CardHeader>
                      <CardTitle>Program Benefits</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="ml-6 list-disc space-y-2">
                        {program.benefits.map((benefit, i) => (
                          <li key={i}>{benefit}</li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>
                </TabsContent>
                <TabsContent value="reviews" className="space-y-4">
                  <Card>
                    <CardHeader>
                      <CardTitle>Reviews & Ratings</CardTitle>
                      <CardDescription>See what other participants say about this program</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="grid gap-6 md:grid-cols-2">
                        <div className="flex flex-col items-center justify-center space-y-2">
                          <div className="text-4xl font-bold">{program.rating}</div>
                          <div className="flex">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <Star
                                key={star}
                                className={`h-5 w-5 ${
                                  star <= Math.round(program.rating) ? "fill-primary text-primary" : "text-muted"
                                }`}
                              />
                            ))}
                          </div>
                          <div className="text-sm text-muted-foreground">Based on {program.reviews.length} reviews</div>
                        </div>
                        <div className="space-y-2">
                          {ratingDistribution.map((item) => (
                            <div key={item.stars} className="flex items-center gap-2">
                              <div className="w-8 text-sm">{item.stars} ★</div>
                              <Progress
                                value={program.reviews.length ? (item.count / program.reviews.length) * 100 : 0}
                                className="h-2"
                              />
                              <div className="w-8 text-sm text-muted-foreground">{item.count}</div>
                            </div>
                          ))}
                        </div>
                      </div>

                      <Separator className="my-6" />

                      <div className="space-y-4">
                        {program.reviews.length > 0 ? (
                          program.reviews.map((review) => (
                            <div key={review.id} className="space-y-2">
                              <div className="flex items-start justify-between">
                                <div className="flex items-center gap-2">
                                  <Avatar>
                                    <AvatarImage src={review.avatar || "/placeholder.svg"} alt={review.user} />
                                    <AvatarFallback>
                                      {review.user
                                        .split(" ")
                                        .map((n) => n[0])
                                        .join("")}
                                    </AvatarFallback>
                                  </Avatar>
                                  <div>
                                    <div className="font-medium">{review.user}</div>
                                    <div className="text-sm text-muted-foreground">
                                      {new Date(review.date).toLocaleDateString()}
                                    </div>
                                  </div>
                                </div>
                                <div className="flex">
                                  {[1, 2, 3, 4, 5].map((star) => (
                                    <Star
                                      key={star}
                                      className={`h-4 w-4 ${
                                        star <= review.rating ? "fill-primary text-primary" : "text-muted"
                                      }`}
                                    />
                                  ))}
                                </div>
                              </div>
                              <p className="text-sm">{review.comment}</p>
                              <Separator className="mt-4" />
                            </div>
                          ))
                        ) : (
                          <div className="text-center py-6">
                            <MessageSquare className="mx-auto h-12 w-12 text-muted-foreground opacity-50" />
                            <h3 className="mt-2 text-lg font-medium">No reviews yet</h3>
                            <p className="text-sm text-muted-foreground">Be the first to review this program</p>
                          </div>
                        )}
                      </div>

                      <div className="mt-6 space-y-4">
                        <h3 className="text-lg font-medium">Write a Review</h3>
                        <div className="flex items-center gap-2">
                          <div className="text-sm">Your rating:</div>
                          <div className="flex">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <button
                                key={star}
                                type="button"
                                onClick={() => setNewRating(star)}
                                className="focus:outline-none"
                              >
                                <Star
                                  className={`h-5 w-5 ${
                                    star <= newRating ? "fill-primary text-primary" : "text-muted hover:text-primary"
                                  }`}
                                />
                              </button>
                            ))}
                          </div>
                        </div>
                        <Textarea
                          placeholder="Share your experience with this program..."
                          value={newReview}
                          onChange={(e) => setNewReview(e.target.value)}
                          rows={4}
                        />
                        <Button onClick={handleSubmitReview}>Submit Review</Button>
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
              </Tabs>
            </div>

            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Contact Information</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <h4 className="font-medium">Contact Person</h4>
                    <p className="text-muted-foreground">{program.contactPerson}</p>
                  </div>
                  <div>
                    <h4 className="font-medium">Email</h4>
                    <p className="text-muted-foreground">{program.contactEmail}</p>
                  </div>
                  <div>
                    <h4 className="font-medium">Website</h4>
                    <a
                      href={program.website}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-primary hover:underline"
                    >
                      Visit program website
                    </a>
                  </div>
                  <Button className="w-full">Contact Organization</Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Application Deadline</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <Calendar className="mr-2 h-5 w-5 text-muted-foreground" />
                      <span>{new Date(program.applicationDeadline).toLocaleDateString()}</span>
                    </div>
                    <Badge variant="outline">Upcoming</Badge>
                  </div>
                  <Button className="mt-4 w-full">Apply Now</Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Report Program</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground">
                    If you find any incorrect information or inappropriate content, please let us know.
                  </p>
                  <Button variant="outline" className="mt-4 w-full" size="sm">
                    <Flag className="mr-2 h-4 w-4" />
                    Report Issue
                  </Button>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}
