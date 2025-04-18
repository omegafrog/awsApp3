import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
    dangerouslyAllowSVG: true,
    remotePatterns: [
      {
        protocol: "http",
        hostname: "img1.kakaocdn.net",
      },
      {
        protocol: "https",
        hostname: "img1.kakaocdn.net",
      },
      {
        protocol: "https",
        hostname: "placehold.co",
      },
      {
        protocol: "http",
        hostname: "k.kakaocdn.net",
      },
      {
        protocol: "http",
        hostname: "localhost",
      },
    ],
    contentSecurityPolicy: "default-src 'self'; img-src 'self' data: https:;",
  },
};

export default nextConfig;
