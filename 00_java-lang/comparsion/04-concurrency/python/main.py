import asyncio
import aiohttp

async def fetch(session, url):
    async with session.get(url) as response:
        print(await response.text())

async def main():
    async with aiohttp.ClientSession() as session:
        tasks = [fetch(session, 'http://example.com') for _ in range(1000)]
        await asyncio.gather(*tasks)

asyncio.run(main())