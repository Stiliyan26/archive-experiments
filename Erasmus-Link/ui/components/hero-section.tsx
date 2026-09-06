import Link from "next/link"
import { Button } from "@/components/ui/button"
import { GlobeIcon, Users2Icon, BookOpenIcon } from "lucide-react"

export function HeroSection() {
  return (
    <section className="w-full py-12 md:py-24 lg:py-32 xl:py-48">
      <div className="container px-4 md:px-6">
        <div className="grid gap-6 lg:grid-cols-[1fr_400px] lg:gap-12 xl:grid-cols-[1fr_600px]">
          <div className="flex flex-col justify-center space-y-4">
            <div className="space-y-2">
              <h1 className="text-3xl font-bold tracking-tighter sm:text-5xl xl:text-6xl/none">
                Connect with Erasmus+ Programs and Organizations
              </h1>
              <p className="max-w-[600px] text-muted-foreground md:text-xl">
                ErasmusLink is your platform for finding, reviewing, and connecting with Erasmus+ programs and
                organizations across Europe.
              </p>
            </div>
            <div className="flex flex-col gap-2 min-[400px]:flex-row">
              <Link href="/register">
                <Button size="lg">Get Started</Button>
              </Link>
              <Link href="/programs">
                <Button variant="outline" size="lg">
                  Explore Programs
                </Button>
              </Link>
            </div>
          </div>
          <div className="flex items-center justify-center">
            <div className="grid grid-cols-2 gap-4 md:grid-cols-2">
              <div className="flex flex-col items-center justify-center space-y-2 rounded-lg border bg-background p-4 shadow-sm">
                <GlobeIcon className="h-12 w-12 text-primary" />
                <h3 className="text-xl font-bold">Global Mobility</h3>
                <p className="text-center text-sm text-muted-foreground">
                  Connect with programs across Europe and beyond
                </p>
              </div>
              <div className="flex flex-col items-center justify-center space-y-2 rounded-lg border bg-background p-4 shadow-sm">
                <Users2Icon className="h-12 w-12 text-primary" />
                <h3 className="text-xl font-bold">Community</h3>
                <p className="text-center text-sm text-muted-foreground">
                  Join a network of students and organizations
                </p>
              </div>
              <div className="flex flex-col items-center justify-center space-y-2 rounded-lg border bg-background p-4 shadow-sm">
                <BookOpenIcon className="h-12 w-12 text-primary" />
                <h3 className="text-xl font-bold">Knowledge</h3>
                <p className="text-center text-sm text-muted-foreground">
                  Access resources and information about programs
                </p>
              </div>
              <div className="flex flex-col items-center justify-center space-y-2 rounded-lg border bg-background p-4 shadow-sm">
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
                  className="h-12 w-12 text-primary"
                >
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                <h3 className="text-xl font-bold">Connections</h3>
                <p className="text-center text-sm text-muted-foreground">Build relationships with program organizers</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
