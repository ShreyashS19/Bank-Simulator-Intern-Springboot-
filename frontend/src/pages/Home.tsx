import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ArrowRight, Shield, TrendingUp, Smartphone, Github, Linkedin, Twitter } from "lucide-react";

const Home = () => {
  return (
    <div className="min-h-screen bg-background">
      {/* Navbar */}
      <nav className="border-b bg-card/50 backdrop-blur-sm sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent"
            >
              Bank Simulation
            </motion.div>
            <div className="hidden md:flex items-center gap-8">
              <a href="#home" className="text-foreground hover:text-primary transition-colors">Home</a>
              <a href="#features" className="text-foreground hover:text-primary transition-colors">Features</a>
              <a href="#about" className="text-foreground hover:text-primary transition-colors">About</a>
              <a href="#contact" className="text-foreground hover:text-primary transition-colors">Contact</a>
            </div>
            <div className="flex gap-3">
              <Link to="/login">
                <Button variant="outline">Login</Button>
              </Link>
              <Link to="/signup">
                <Button>Sign Up</Button>
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section id="home" className="relative overflow-hidden py-20 md:py-32">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-secondary/5 to-accent/10" />
        <div className="container mx-auto px-4 relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-center max-w-4xl mx-auto"
          >
            <h1 className="text-5xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Experience Digital Banking

            </h1>
            <p className="text-xl md:text-2xl text-muted-foreground mb-8">
              Simple, Smart, and Secure.
            </p>
            <p className="text-lg text-muted-foreground mb-10 max-w-2xl mx-auto">
              Manage your finances with confidence. Access real-time insights, secure transfers, and smart banking tools all in one place.
            </p>
            <Link to="/login">
              <Button size="lg" className="group">
                Get Started
                <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-muted/30">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl font-bold mb-4">Why Choose Bank Simulation?</h2>
            <p className="text-muted-foreground text-lg">Modern banking features designed for the digital age</p>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: index * 0.1 }}
                className="bg-card p-8 rounded-lg border shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="h-12 w-12 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center mb-4">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
                <p className="text-muted-foreground">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20">
        <div className="container mx-auto px-4">
          <div className="max-w-3xl mx-auto text-center">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
            >
              <h2 className="text-4xl font-bold mb-6">About Bank Simulation</h2>
              <p className="text-lg text-muted-foreground mb-6">
                Bank Simulation is a modern banking simulation platform built with cutting-edge technology to provide a seamless digital banking experience.
              </p>
              <p className="text-lg text-muted-foreground">
                Our platform combines security, speed, and simplicity to help you manage your finances efficiently. Whether you're tracking transactions, managing accounts, or analyzing financial data, Bank Simulation provides the tools you need.
              </p>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section id="contact" className="py-20 bg-muted/30">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center"
          >
            <h2 className="text-4xl font-bold mb-4">Get In Touch</h2>
            <p className="text-muted-foreground mb-8">Have questions? We'd love to hear from you.</p>
            <div className="flex justify-center gap-6">
              <a href="mailto:bank.simulator.issue@gmail.com" className="text-primary hover:text-secondary transition-colors">
                bank.simulator.issue@gmail.com
              </a>
              <span className="text-muted-foreground">|</span>
              <a href="tel:+1234567890" className="text-primary hover:text-secondary transition-colors">
                +1 (234) 567-890
              </a>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t bg-card/50 backdrop-blur-sm py-8">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-muted-foreground">
              © 2025 Bank Simulation. All rights reserved.
            </p>
            <div className="flex gap-4">
              <a href="https://github.com" target="_blank" rel="noopener noreferrer" className="text-muted-foreground hover:text-primary transition-colors">
                <Github className="h-5 w-5" />
              </a>
              <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" className="text-muted-foreground hover:text-primary transition-colors">
                <Linkedin className="h-5 w-5" />
              </a>
              <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" className="text-muted-foreground hover:text-primary transition-colors">
                <Twitter className="h-5 w-5" />
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

const features = [
  {
    icon: <Shield className="h-6 w-6 text-white" />,
    title: "Secure Transfers",
    description: "Bank-grade encryption ensures your transactions are always protected and secure."
  },
  {
    icon: <TrendingUp className="h-6 w-6 text-white" />,
    title: "Real-Time Insights",
    description: "Track your finances with live updates and comprehensive analytics dashboards."
  },
  {
    icon: <Smartphone className="h-6 w-6 text-white" />,
    title: "Smart Banking",
    description: "Access your accounts anywhere, anytime with our responsive banking platform."
  }
];

export default Home;
