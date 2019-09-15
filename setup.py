from distutils.util import convert_path
from setuptools import setup, find_packages
from os import path


here = path.abspath(path.dirname(__file__))
metadata = dict()
with open(convert_path('src/ffxivbis/core/version.py')) as metadata_file:
    exec(metadata_file.read(), metadata)


setup(
    name='ffxivbis',

    version=metadata['__version__'],
    zip_safe=False,

    description='Helper to handle loot drop',

    author='Evgeniy Alekseev',
    author_email='i@arcanis.me',

    license='BSD',

    package_dir={'': 'src'},
    packages=find_packages(where='src', exclude=['contrib', 'docs', 'test']),

    install_requires=[
        'aiohttp==3.6.0',
        'aiohttp_jinja2',
        'aiohttp_security',
        'apispec',
        'iniherit',
        'Jinja2',
        'passlib',
        'yoyo_migrations'
    ],
    setup_requires=[
        'pytest-runner'
    ],
    tests_require=[
        'pytest', 'pytest-aiohttp', 'pytest-asyncio'
    ],

    include_package_data=True,

    extras_require={
        'Postgresql': ['asyncpg'],
        'SQLite':  ['aiosqlite'],
        'test': ['coverage', 'pytest'],
    },
)
