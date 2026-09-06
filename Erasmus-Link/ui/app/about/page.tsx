import Link from "next/link"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { GlobeIcon, Users2Icon, StarIcon, MessageSquareIcon, ShieldCheckIcon } from "lucide-react"

export default function AboutPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero Section */}
        <section className="bg-muted/50 py-12 md:py-16 lg:py-20">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl text-center">
              <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">About ErasmusLink</h1>
              <p className="mt-4 text-muted-foreground md:text-xl">
                Connecting Erasmus+ participants and organizations across Europe
              </p>
            </div>
          </div>
        </section>

        {/* Mission Section */}
        <section className="py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl">Our Mission</h2>
              <p className="mt-4 text-muted-foreground">
                ErasmusLink was created with a clear mission: to improve and simplify communication between Erasmus+
                program organizers and participants. We believe that access to reliable information and direct
                communication channels can significantly enhance the Erasmus experience for everyone involved.
              </p>
              <p className="mt-4 text-muted-foreground">
                Our platform serves as a centralized hub where participants can find verified information about
                programs, read reviews from past participants, and connect directly with organizers. For organizations,
                we provide a space to showcase their programs, build their reputation through ratings and reviews, and
                efficiently communicate with potential and current participants.
              </p>
            </div>
          </div>
        </section>

        {/* How It Works Section */}
        <section className="bg-muted/30 py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-4xl text-center">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl">How ErasmusLink Works</h2>
              <p className="mt-4 text-muted-foreground md:text-xl">
                Our platform connects students, educational institutions, and program organizers in one place
              </p>
            </div>

            <div className="mt-12 grid gap-8 md:grid-cols-3">
              <Card className="bg-background">
                <CardHeader className="text-center">
                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                    <Users2Icon className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle className="mt-4">For Participants</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-muted-foreground">
                    <li>Search for programs that match your interests</li>
                    <li>Read reviews from past participants</li>
                    <li>Connect directly with program organizers</li>
                    <li>Share your experiences through ratings and reviews</li>
                  </ul>
                </CardContent>
              </Card>

              <Card className="bg-background">
                <CardHeader className="text-center">
                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                    <GlobeIcon className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle className="mt-4">For Organizations</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-muted-foreground">
                    <li>Create and manage program listings</li>
                    <li>Build reputation through verified reviews</li>
                    <li>Communicate with potential participants</li>
                    <li>Showcase your institution's strengths</li>
                  </ul>
                </CardContent>
              </Card>

              <Card className="bg-background">
                <CardHeader className="text-center">
                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                    <ShieldCheckIcon className="h-6 w-6 text-primary" />
                  </div>
                  <CardTitle className="mt-4">Trust & Transparency</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2 text-muted-foreground">
                    <li>Verification of organizations and programs</li>
                    <li>Authentic reviews from real participants</li>
                    <li>Moderation of content for quality</li>
                    <li>Secure communication channels</li>
                  </ul>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* Key Features Section */}
        <section className="py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-4xl text-center">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl">Key Features</h2>
              <p className="mt-4 text-muted-foreground md:text-xl">
                ErasmusLink offers a comprehensive set of tools to enhance the Erasmus+ experience
              </p>
            </div>

            <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <StarIcon className="h-6 w-6 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Ratings & Reviews</h3>
                <p className="text-center text-muted-foreground">
                  Transparent feedback system to help participants make informed decisions
                </p>
              </div>

              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <MessageSquareIcon className="h-6 w-6 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Direct Communication</h3>
                <p className="text-center text-muted-foreground">
                  Chat directly with program organizers and other participants
                </p>
              </div>

              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <ShieldCheckIcon className="h-6 w-6 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Verified Organizations</h3>
                <p className="text-center text-muted-foreground">
                  All organizations are verified to ensure authenticity and trust
                </p>
              </div>

              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="h-6 w-6 text-primary"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                  </svg>
                </div>
                <h3 className="text-xl font-bold">Community Forums</h3>
                <p className="text-center text-muted-foreground">
                  Connect with other students and share experiences in topic-based discussions
                </p>
              </div>

              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="h-6 w-6 text-primary"
                  >
                    <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <h3 className="text-xl font-bold">User Profiles</h3>
                <p className="text-center text-muted-foreground">
                  Personalized profiles for both participants and organizations
                </p>
              </div>

              <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
                <div className="rounded-full bg-primary/10 p-3">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="h-6 w-6 text-primary"
                  >
                    <path d="m21 11-8-8-8 8"></path>
                    <path d="M21 22v-4a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v4"></path>
                    <path d="M7 10.5V4h10v6.5"></path>
                  </svg>
                </div>
                <h3 className="text-xl font-bold">Program Management</h3>
                <p className="text-center text-muted-foreground">
                  Tools for organizations to create and manage their program listings
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Team Section */}
        <section className="bg-muted/30 py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-4xl text-center">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl">Meet Our Team</h2>
              <p className="mt-4 text-muted-foreground md:text-xl">The passionate individuals behind ErasmusLink</p>
            </div>

            <div className="mt-12 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
              <div className="flex flex-col items-center text-center">
                <Avatar className="h-24 w-24">
                  <AvatarImage src="/placeholder.svg?height=96&width=96" alt="Kalina Marinova" />
                  <AvatarFallback>KM</AvatarFallback>
                </Avatar>
                <h3 className="mt-4 text-lg font-semibold">Kalina Marinova</h3>
                <p className="text-sm text-muted-foreground">Co-founder & CEO</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  Former Erasmus participant with a passion for connecting students across borders
                </p>
              </div>

              <div className="flex flex-col items-center text-center">
                <Avatar className="h-24 w-24">
                  <AvatarImage src="/placeholder.svg?height=96&width=96" alt="Stiliyan Nikolov" />
                  <AvatarFallback>SN</AvatarFallback>
                </Avatar>
                <h3 className="mt-4 text-lg font-semibold">Stiliyan Nikolov</h3>
                <p className="text-sm text-muted-foreground">Co-founder & CTO</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  Tech enthusiast focused on building accessible and user-friendly platforms
                </p>
              </div>

              <div className="flex flex-col items-center text-center">
                <Avatar className="h-24 w-24">
                  <AvatarImage src="/placeholder.svg?height=96&width=96" alt="Vasilena Slavova" />
                  <AvatarFallback>VS</AvatarFallback>
                </Avatar>
                <h3 className="mt-4 text-lg font-semibold">Vasilena Slavova</h3>
                <p className="text-sm text-muted-foreground">Head of Partnerships</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  Building relationships with educational institutions across Europe
                </p>
              </div>

              <div className="flex flex-col items-center text-center">
                <Avatar className="h-24 w-24">
                  <AvatarImage src="/placeholder.svg?height=96&width=96" alt="Venislav Kirilov" />
                  <AvatarFallback>VK</AvatarFallback>
                </Avatar>
                <h3 className="mt-4 text-lg font-semibold">Venislav Kirilov</h3>
                <p className="text-sm text-muted-foreground">UX Designer</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  Creating intuitive and accessible experiences for all users
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* FAQ Section */}
        <section className="py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl text-center">Frequently Asked Questions</h2>

              <Tabs defaultValue="participants" className="mt-8">
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="participants">For Participants</TabsTrigger>
                  <TabsTrigger value="organizations">For Organizations</TabsTrigger>
                </TabsList>
                <TabsContent value="participants" className="mt-4">
                  <Accordion type="single" collapsible className="w-full">
                    <AccordionItem value="item-1">
                      <AccordionTrigger>Is ErasmusLink free to use for students?</AccordionTrigger>
                      <AccordionContent>
                        Yes, ErasmusLink is completely free for Erasmus+ participants. You can create an account, search
                        for programs, read reviews, and communicate with organizations at no cost.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-2">
                      <AccordionTrigger>How do I know if an organization is legitimate?</AccordionTrigger>
                      <AccordionContent>
                        All organizations on ErasmusLink go through a verification process. Look for the verification
                        badge on organization profiles. Additionally, reviews from past participants can provide
                        insights into the organization's reputation.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-3">
                      <AccordionTrigger>Can I apply for programs directly through ErasmusLink?</AccordionTrigger>
                      <AccordionContent>
                        While you can express interest and initiate contact with organizations through ErasmusLink, the
                        formal application process typically takes place through the organization's official channels or
                        the Erasmus+ application system.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-4">
                      <AccordionTrigger>How can I leave a review for a program I participated in?</AccordionTrigger>
                      <AccordionContent>
                        After completing a program, you can visit the program's page and click on the "Write a Review"
                        section. Your review will help future participants make informed decisions.
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                </TabsContent>
                <TabsContent value="organizations" className="mt-4">
                  <Accordion type="single" collapsible className="w-full">
                    <AccordionItem value="item-1">
                      <AccordionTrigger>How can my organization join ErasmusLink?</AccordionTrigger>
                      <AccordionContent>
                        You can register as an organization and create a profile. Our team will then verify your
                        organization's credentials before your profile becomes fully visible to participants.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-2">
                      <AccordionTrigger>Is there a cost for organizations to use ErasmusLink?</AccordionTrigger>
                      <AccordionContent>
                        Basic features are free for all organizations. We also offer premium features for enhanced
                        visibility and additional tools, available through our subscription plans.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-3">
                      <AccordionTrigger>How can we respond to reviews about our programs?</AccordionTrigger>
                      <AccordionContent>
                        Organizations can respond to any review left for their programs. This allows you to address
                        feedback, provide additional context, or thank participants for their positive comments.
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="item-4">
                      <AccordionTrigger>Can we update our program listings after publishing?</AccordionTrigger>
                      <AccordionContent>
                        Yes, you can update your program listings at any time. We encourage keeping your information
                        current to provide participants with the most accurate details.
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                </TabsContent>
              </Tabs>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="bg-primary/5 py-12 md:py-16">
          <div className="container px-4 md:px-6">
            <div className="mx-auto max-w-3xl text-center">
              <h2 className="text-2xl font-bold tracking-tight md:text-3xl">Ready to Get Started?</h2>
              <p className="mt-4 text-muted-foreground md:text-xl">
                Join ErasmusLink today and become part of our growing community of Erasmus+ participants and
                organizations
              </p>
              <div className="mt-8 flex flex-col gap-4 sm:flex-row sm:justify-center">
                <Button asChild size="lg">
                  <Link href="/register">Create an Account</Link>
                </Button>
                <Button asChild variant="outline" size="lg">
                  <Link href="/programs">Explore Programs</Link>
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
