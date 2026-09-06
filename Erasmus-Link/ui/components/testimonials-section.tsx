import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Card, CardContent, CardHeader } from "@/components/ui/card"

export function TestimonialsSection() {
  return (
    <section className="w-full py-12 md:py-24 lg:py-32">
      <div className="container px-4 md:px-6">
        <div className="flex flex-col items-center justify-center space-y-4 text-center">
          <div className="space-y-2">
            <div className="inline-block rounded-lg bg-muted px-3 py-1 text-sm">Testimonials</div>
            <h2 className="text-3xl font-bold tracking-tighter md:text-4xl/tight">Hear from our community</h2>
            <p className="max-w-[900px] text-muted-foreground md:text-xl/relaxed lg:text-base/relaxed xl:text-xl/relaxed">
              Students and organizations share their experiences with ErasmusLink
            </p>
          </div>
        </div>
        <div className="mx-auto grid max-w-5xl grid-cols-1 gap-6 py-12 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center gap-4 pb-2">
              <Avatar>
                <AvatarImage alt="User" src="/placeholder.svg?height=40&width=40" />
                <AvatarFallback>MK</AvatarFallback>
              </Avatar>
              <div className="grid gap-1">
                <h4 className="text-lg font-semibold">Maria Kovács</h4>
                <p className="text-sm text-muted-foreground">Student, University of Budapest</p>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                "ErasmusLink helped me find the perfect program in Barcelona. The reviews from other students were
                invaluable in making my decision."
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center gap-4 pb-2">
              <Avatar>
                <AvatarImage alt="User" src="/placeholder.svg?height=40&width=40" />
                <AvatarFallback>JS</AvatarFallback>
              </Avatar>
              <div className="grid gap-1">
                <h4 className="text-lg font-semibold">Jan Schmidt</h4>
                <p className="text-sm text-muted-foreground">Program Coordinator, Berlin Institute</p>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                "As an organization, we've been able to connect with motivated students from across Europe. The platform
                makes communication seamless."
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center gap-4 pb-2">
              <Avatar>
                <AvatarImage alt="User" src="/placeholder.svg?height=40&width=40" />
                <AvatarFallback>SL</AvatarFallback>
              </Avatar>
              <div className="grid gap-1">
                <h4 className="text-lg font-semibold">Sofia Lopez</h4>
                <p className="text-sm text-muted-foreground">Student, University of Madrid</p>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                "The chat feature allowed me to ask questions directly to program organizers before applying. It made
                the whole process much less stressful."
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </section>
  )
}
