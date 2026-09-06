import { MessageSquareIcon, SearchIcon, StarIcon, ShieldCheckIcon, UsersIcon, GlobeIcon } from "lucide-react"

export function FeaturesSection() {
  return (
    <section className="w-full py-12 md:py-24 lg:py-32 bg-muted/50">
      <div className="container px-4 md:px-6">
        <div className="flex flex-col items-center justify-center space-y-4 text-center">
          <div className="space-y-2">
            <div className="inline-block rounded-lg bg-muted px-3 py-1 text-sm">Features</div>
            <h2 className="text-3xl font-bold tracking-tighter md:text-4xl/tight">
              Everything you need for your Erasmus journey
            </h2>
            <p className="max-w-[900px] text-muted-foreground md:text-xl/relaxed lg:text-base/relaxed xl:text-xl/relaxed">
              ErasmusLink provides a comprehensive platform for students and organizations to connect, share
              experiences, and find the perfect Erasmus+ program.
            </p>
          </div>
        </div>
        <div className="mx-auto grid max-w-5xl grid-cols-1 gap-6 py-12 md:grid-cols-2 lg:grid-cols-3">
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
              <SearchIcon className="h-6 w-6 text-primary" />
            </div>
            <h3 className="text-xl font-bold">Advanced Search</h3>
            <p className="text-center text-muted-foreground">Find programs by location, type, rating, and more</p>
          </div>
          <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
            <div className="rounded-full bg-primary/10 p-3">
              <StarIcon className="h-6 w-6 text-primary" />
            </div>
            <h3 className="text-xl font-bold">Ratings & Reviews</h3>
            <p className="text-center text-muted-foreground">
              Read and leave reviews to help others make informed decisions
            </p>
          </div>
          <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
            <div className="rounded-full bg-primary/10 p-3">
              <ShieldCheckIcon className="h-6 w-6 text-primary" />
            </div>
            <h3 className="text-xl font-bold">Verified Organizations</h3>
            <p className="text-center text-muted-foreground">All organizations are verified to ensure authenticity</p>
          </div>
          <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
            <div className="rounded-full bg-primary/10 p-3">
              <UsersIcon className="h-6 w-6 text-primary" />
            </div>
            <h3 className="text-xl font-bold">Community Forums</h3>
            <p className="text-center text-muted-foreground">Connect with other students and share experiences</p>
          </div>
          <div className="flex flex-col items-center space-y-2 rounded-lg border p-6 shadow-sm">
            <div className="rounded-full bg-primary/10 p-3">
              <GlobeIcon className="h-6 w-6 text-primary" />
            </div>
            <h3 className="text-xl font-bold">Multilingual Support</h3>
            <p className="text-center text-muted-foreground">
              Platform available in multiple languages for accessibility
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
